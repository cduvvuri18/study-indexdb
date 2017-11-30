package com.cduvvuri.sidb.validate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.cduvvuri.sidb.error.ValidationException;
import com.cduvvuri.sidb.persistent.btree.BTree.CreateRequest;

/**
 * 
 * @author Chaitanya DS
 * 13-Nov-2017
 */
//Validate the request
public class BTreeRequestValidator<K extends Comparable<K>, E> implements Validator<CreateRequest<K, E>> {

	private CreateRequest<K, E> request;

	@Override
	public Result validate(CreateRequest<K, E> request) {
		this.request = request;
		try {
			validatePath();
			validateEntity();			
			validateKey();
			
			if (!request.isnew) {
				validateIfIdxExists();
			}
		} catch (ValidationException e) {
			return new SimpleResult(false, e.getMessage());
		}
		return Result.OK;
	}

	/**
	 * Validates the key
	 * throws ValidationException if invalid
	 */
	private void validateKey() {
		Result result = new KeyValidator<K>().validate(request.key);
		
		if (!result.isOk()) {
			//validate the entity
			throw new ValidationException(result.getMessage());
		}
	}

	/**
	 * While opening the BTree, it is expected that index file should exist.
	 * throws ValidationException If index file do not exist.
	 */
	private void validateIfIdxExists() {	
		String path = request.dbpath.toUri().getPath() + File.pathSeparator + request.entity.getSimpleName()+".idx";
		
		if(Files.exists(Paths.get(path))) {
			throw new ValidationException("Index file is expected @ the given path :: "+path);
		}
	}

	/**
	 * Validates the given path
	 * throws ValidationException If path is incorrect
	 */
	private void validatePath() {
		Result result = new PathValidator().validate(request.dbpath);

		if (!result.isOk()) {			
			throw new ValidationException(result.getMessage());
		}
	}

	private void validateEntity() {		
		Result result = new EntityValidator<E>().validate(request.entity);

		if (!result.isOk()) {
			//validate the entity
			throw new ValidationException(result.getMessage());
		}
	}
}
