package tp2.impl.servers.rest;

import tp2.api.FileInfo;

public  record ZookeeperRequest(String request, FileInfo info) {
}
