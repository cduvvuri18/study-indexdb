package com.cduvvuri.sidb.validate;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 
 * @author Chaitanya DS
 * 12-Nov-2017
 */
public class PathValidator implements Validator<Path> {
	public Result validate(Path path) {
		
		if(path == null) {
			return new SimpleResult(false, "path is null");
		}
		
		if(!Files.exists(path)) {
			return new SimpleResult(false, "Path does not exist @ "+path);
		}
		
		if(!Files.isDirectory(path)) {
			return new SimpleResult(false, "Path is not the directory, "+path);
		}
		
		return new SimpleResult(true, "");
	}
	
}
