package tp2.impl.clients.rest;

public record UploadFileArgs(String path, String mode, Boolean autorename, Boolean mute, Boolean strict_conflict) {

}
