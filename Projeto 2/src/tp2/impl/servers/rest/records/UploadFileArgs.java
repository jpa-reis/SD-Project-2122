package tp2.impl.servers.rest.records;

public record UploadFileArgs(String path, String mode, Boolean autorename, Boolean mute, Boolean strict_conflict) {

}
