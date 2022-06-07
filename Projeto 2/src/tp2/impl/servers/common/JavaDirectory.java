package tp2.impl.servers.common;

import static tp2.api.service.java.Result.error;
import static tp2.api.service.java.Result.ok;
import static tp2.api.service.java.Result.redirect;
import static tp2.api.service.java.Result.ErrorCode.BAD_REQUEST;
import static tp2.api.service.java.Result.ErrorCode.FORBIDDEN;
import static tp2.api.service.java.Result.ErrorCode.NOT_FOUND;
import static tp2.impl.clients.Clients.FilesClients;
import static tp2.impl.clients.Clients.UsersClients;

import java.io.Serializable;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.SerializationUtils;
import tp2.api.FileInfo;
import tp2.api.User;
import tp2.api.service.java.Directory;
import tp2.api.service.java.Result;
import tp2.api.service.java.Result.ErrorCode;
import tp2.impl.zookeeper.Zookeeper;
import util.Token;

public class JavaDirectory implements Directory {
	static final long USER_CACHE_EXPIRATION = 3000;
	public static URI lastGoodServer;
	final LoadingCache<UserInfo, Result<User>> users = CacheBuilder.newBuilder()
			.expireAfterWrite( Duration.ofMillis(USER_CACHE_EXPIRATION))
			.build(new CacheLoader<>() {
				@Override
				public Result<User> load(UserInfo info) throws Exception {
					var res = UsersClients.get().getUser( info.userId(), info.password());
					if( res.error() == ErrorCode.TIMEOUT)
						return error(BAD_REQUEST);
					else
						return res;
				}
			});
	
	final static Logger Log = Logger.getLogger(JavaDirectory.class.getName());
	final ExecutorService executor = Executors.newCachedThreadPool();

	final Map<String, ExtendedFileInfo> files = new ConcurrentHashMap<>();
	final Map<String, UserFiles> userFiles = new ConcurrentHashMap<>();
	final Map<URI, FileCounts> fileCounts = new ConcurrentHashMap<>();
	final Map<URI, Integer> serverCapacity = new ConcurrentHashMap<>();

	@Override
	public Result<FileInfo> writeFile(String filename, byte[] data, String userId, String password) throws Exception {

		if (badParam(filename) || badParam(userId))
			return error(BAD_REQUEST);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.computeIfAbsent(userId, (k) -> new UserFiles());
		synchronized (uf) {
			var fileId = fileId(filename, userId);
			var file = files.get(fileId);
			var info = file != null ? file.info() : new FileInfo();
			int counter = 0;

			//add newfound servers
			for (var uri :  FilesClients.all()) {
				if(!serverCapacity.containsKey(uri)) {
					serverCapacity.put(uri, 0);
				}
			}
			for (var uri :  getServerByCapacity()) {
				var result = FilesClients.get(uri).writeFile(fileId, data, DigestUtils.sha512Hex(Token.get()));
				if (result.isOK()) {
					if(!files.containsKey(fileId)) {
						info.setOwner(userId);
						info.setFilename(filename);
						info.setFileURL(String.format("%s/files/%s", uri, fileId));
						files.put(fileId, file = new ExtendedFileInfo(uri, fileId, info));

						var zk = new Zookeeper("kafka");
						zk.client().setData("/directory/"+zk.getChildren("/directory").stream().sorted().toList().get(0),
											SerializationUtils.serialize(info),
											-1);

						if( uf.owned().add(fileId))
							getFileCounts(file.uri(), true).numFiles().incrementAndGet();
					}
					int oldCapacity = serverCapacity.get(uri);
					oldCapacity++;
					serverCapacity.replace(uri, oldCapacity);
					counter++;
					if(counter == 2 || serverCapacity.size() == 1) return ok(file.info());
				} else{
					Log.info(String.format("Files.writeFile(...) to %s failed with: %s \n", uri, result));
				}
			}
			return error(BAD_REQUEST);
		}
	}

	private List<URI> getServerByCapacity() {
		return serverCapacity.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.naturalOrder()))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());
	}

	@Override
	public Result<FileInfo> writeFileSecondary(String filename, byte[] infoBytes){

		FileInfo info = SerializationUtils.deserialize(infoBytes);

		var uf = userFiles.computeIfAbsent(info.getOwner(), (k) -> new UserFiles());
		synchronized (uf) {
			String fileId = fileId(filename, info.getOwner());
			var file = files.get(fileId);
			if(!files.containsKey(fileId)) {
				files.put(fileId, file = new ExtendedFileInfo(URI.create(info.getFileURL().split("/files/")[0]), fileId, info));
				if( uf.owned().add(fileId))
					getFileCounts(file.uri(), true).numFiles().incrementAndGet();
			}
			return ok(info);
		}

	}

	
	@Override
	public Result<Void> deleteFile(String filename, String userId, String password) {
		if (badParam(filename) || badParam(userId))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);

		var file = files.get(fileId);
		if (file == null)
			return error(NOT_FOUND);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.getOrDefault(userId, new UserFiles());
		synchronized (uf) {
			var info = files.remove(fileId);
			uf.owned().remove(fileId);

			executor.execute(() -> {
				this.removeSharesOfFile(info);
				FilesClients.get(file.uri()).deleteFile(fileId, DigestUtils.sha512Hex(Token.get()));
			});
			
			getFileCounts(info.uri(), false).numFiles().decrementAndGet();
		}
		return ok();
	}

	@Override
	public Result<Void> shareFile(String filename, String userId, String userIdShare, String password) {
		if (badParam(filename) || badParam(userId) || badParam(userIdShare))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);

		var file = files.get(fileId);
		if (file == null || getUser(userIdShare, "").error() == NOT_FOUND)
			return error(NOT_FOUND);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.computeIfAbsent(userIdShare, (k) -> new UserFiles());
		synchronized (uf) {
			uf.shared().add(fileId);
			file.info().getSharedWith().add(userIdShare);
		}

		return ok();
	}

	@Override
	public Result<Void> unshareFile(String filename, String userId, String userIdShare, String password) {
		if (badParam(filename) || badParam(userId) || badParam(userIdShare))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);

		var file = files.get(fileId);
		if (file == null || getUser(userIdShare, "").error() == NOT_FOUND)
			return error(NOT_FOUND);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.computeIfAbsent(userIdShare, (k) -> new UserFiles());
		synchronized (uf) {
			uf.shared().remove(fileId);
			file.info().getSharedWith().remove(userIdShare);
		}

		return ok();
	}

	@Override
	public Result<byte[]> getFile(String filename, String userId, String accUserId, String password) {
		if (badParam(filename))
			return error(BAD_REQUEST);

		var fileId = fileId(filename, userId);
		var file = files.get(fileId);
		if (file == null)
			return error(NOT_FOUND);

		var user = getUser(accUserId, password);
		if (!user.isOK())
			return error(user.error());

		if (!file.info().hasAccess(accUserId))
			return error(FORBIDDEN);

		if(lastGoodServer == null) return redirect(file.info().getFileURL());
		else return redirect(String.format("%s/files/%s", lastGoodServer, fileId));
	}

	@Override
	public Result<List<FileInfo>> lsFile(String userId, String password) {
		if (badParam(userId))
			return error(BAD_REQUEST);

		var user = getUser(userId, password);
		if (!user.isOK())
			return error(user.error());

		var uf = userFiles.getOrDefault(userId, new UserFiles());
		synchronized (uf) {
			var infos = Stream.concat(uf.owned().stream(), uf.shared().stream()).map(f -> files.get(f).info())
					.collect(Collectors.toSet());

			return ok(new ArrayList<>(infos));
		}
	}

	public static String fileId(String filename, String userId) {
		return userId + JavaFiles.DELIMITER + filename;
	}

	private static boolean badParam(String str) {
		return str == null || str.length() == 0;
	}

	private Result<User> getUser(String userId, String password) {
		try {
			return users.get( new UserInfo( userId, password));
		} catch( Exception x ) {
			x.printStackTrace();
			return error( ErrorCode.INTERNAL_ERROR);
		}
	}
	
	@Override
	public Result<Void> deleteUserFiles(String userId, String password, String token) {
		verifyToken(token);
		users.invalidate( new UserInfo(userId, password));
		
		var fileIds = userFiles.remove(userId);
		if (fileIds != null)
			for (var id : fileIds.owned()) {
				var file = files.remove(id);
				removeSharesOfFile(file);
				getFileCounts(file.uri(), false).numFiles().decrementAndGet();
			}
		return ok();
	}

	private void removeSharesOfFile(ExtendedFileInfo file) {
		for (var userId : file.info().getSharedWith())
			userFiles.getOrDefault(userId, new UserFiles()).shared().remove(file.fileId());
	}


	private Queue<URI> orderCandidateFileServers(ExtendedFileInfo file) {
		int MAX_SIZE=3;
		Queue<URI> result = new ArrayDeque<>();
		
		if( file != null )
			result.add( file.uri() );

		FilesClients.all()
				.stream()
				.filter( u -> ! result.contains(u))
				.map(u -> getFileCounts(u, false))
				.sorted( FileCounts::ascending )
				.map(FileCounts::uri)
				.limit(MAX_SIZE)
				.forEach( result::add );
		
		while( result.size() < MAX_SIZE )
			result.add( result.peek() );
		
		Log.info("Candidate files servers: " + result+ "\n");
		return result;
	}
	
	private FileCounts getFileCounts( URI uri, boolean create ) {
		if( create )
			return fileCounts.computeIfAbsent(uri,  FileCounts::new);
		else
			return fileCounts.getOrDefault( uri, new FileCounts(uri) );
	}
	
	static record ExtendedFileInfo(URI uri, String fileId, FileInfo info) implements Serializable {
		@Override
		public boolean equals(Object obj) {
			return false;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public String toString() {
			return null;
		}
	}

	static record UserFiles(Set<String> owned, Set<String> shared) {

		UserFiles() {
			this(ConcurrentHashMap.newKeySet(), ConcurrentHashMap.newKeySet());
		}
	}

	static record FileCounts(URI uri, AtomicLong numFiles) {
		FileCounts( URI uri) {
			this(uri, new AtomicLong(0L) );
		}

		static int ascending(FileCounts a, FileCounts b) {
			return Long.compare( a.numFiles().get(), b.numFiles().get());
		}
	}	
	
	static record UserInfo(String userId, String password) {		
	}

	private void verifyToken(String token){
		if(!DigestUtils.sha512Hex(Token.get()).equals(token)){
			throw new WebApplicationException(Response.Status.FORBIDDEN);
		}
	}
}