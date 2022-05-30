package tp2.server.factory_clients.files_clients;

public record GetFileArgs(String path, boolean include_media_info, boolean include_deleted, boolean include_has_explicit_shared_members) {
    
}
