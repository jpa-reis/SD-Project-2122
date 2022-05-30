package tp2.server.factory_clients.files_clients;

public record UploadFileArgs(String path, String mode, boolean autorename, boolean mute, boolean strict_conflict, byte[] content_hash) {
    
}
