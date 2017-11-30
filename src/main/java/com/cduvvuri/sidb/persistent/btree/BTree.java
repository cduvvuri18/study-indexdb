package com.cduvvuri.sidb.persistent.btree;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.cduvvuri.sidb.error.BTreeException;
import com.cduvvuri.sidb.error.ValidationException;
import com.cduvvuri.sidb.index.DBIndex;
import com.cduvvuri.sidb.logger.ILogger;
import com.cduvvuri.sidb.validate.BTreeRequestValidator;
import com.cduvvuri.sidb.validate.Validator.Result;

/**
 * V1.0 Many things to do.
 * @author Chaitanya DS
 * 08-Nov-2017
 */
public class BTree<K extends Comparable<K>, E> implements DBIndex<K, E> {
	CreateRequest<K, E> request;
	Header<K, E> header;
	RandomAccessFile accessFile;
	FileChannel fileChannel;
	Path filePath;
	Node<K, E> rootNode;
	Insert<K, E> insert;
	Search<K, E> search;
	Successor<K, E> successor;
	Delete<K, E> delete;

	public static class CreateRequest<K, E> {
		public final Class<E> entity;
		public final Class<K> key;
		public final Path dbpath;
		public final boolean isnew;

		public CreateRequest(Class<K> key, Class<E> entity, Path dbpath, boolean isnew) {
			this.entity = entity;
			this.key = key;
			this.dbpath = dbpath;
			this.isnew = isnew;
		}
	}

	/**
	 * 
	 * @param entity
	 * @param dbpath
	 * @return 
	 * @exception throws ValidationException If dbpath do not exist or If it is not a directory
	 * @exception throws BTreeException which wraps any other error cause
	 */
	public BTree(CreateRequest<K, E> request) {
		this.request = request;

		ILogger.info("create dbpath @ " + this.request.dbpath);
		ILogger.info("create btree for the entity, " + this.request.entity.getSimpleName());
		ILogger.info("create btree for the entity, " + this.request.key.getSimpleName());

		//validate the request
		Result result = new BTreeRequestValidator<K, E>().validate(this.request);
		if (!result.isOk()) {
			throw new ValidationException(result.getMessage());
		}

		//set the file path
		this.filePath = Paths.get(request.dbpath.toUri().getPath() + request.entity.getSimpleName() + ".idx");

		//Delete the index if exists and the request is for new index
		deleteIfIndexExists();
		
		//Initialize the file channel
		initFileChannel();

		//Initialize the header
		initHeader();
	}

	//allocate node
	Node<K, E> allocateNode() throws IOException {
		Node<K, E> newNode = new Node<K, E>(header.order);
		newNode.fpos = fileChannel.size();
		accessFile.setLength(newNode.fpos + header.nodeSize);
		return newNode;
	}

	//Initialize the root node
	private void initRootNode() {
		try {
			//Delete the index If already exists
			if (request.isnew) {
				this.rootNode = allocateNode();
				this.rootNode.isLeaf = true;
				this.rootNode.write(fileChannel, header);
			} else {
				this.rootNode = new Node<K, E>(header.order);
				this.rootNode.fpos = header.rootPos;
				this.rootNode.read(fileChannel, header);
			}
		} catch (IOException e) {
			throw new BTreeException(e.getMessage(), e);
		}
	}

	private void initHeader() {
		try {
			//Create the header using the data from entity. 
			this.header = new Header<K, E>(this.request.key, this.request.entity);

			if (request.isnew) {
				this.header.write(fileChannel);
			} else {
				//Index is not new, compare the header with the physical header embedded in the file
				this.header.read(fileChannel);
			}
		} catch (IOException e) {
			throw new BTreeException(e.getMessage(), e);
		}
	}

	private void deleteIfIndexExists() {
		if (!request.isnew) {
			return;
		}
		try {
			//Delete the index If already exists
			Files.deleteIfExists(filePath);
		} catch (IOException e) {
			throw new BTreeException(e.getMessage(), e);
		}
	}

	//Initialize the file channel
	private void initFileChannel() {
		try {
			accessFile = new RandomAccessFile(this.filePath.toFile(), "rw");
			this.fileChannel = accessFile.getChannel();
		} catch (FileNotFoundException f) {
			throw new BTreeException(f.getMessage(), f);
		}
	}

	/**
	 * @retrun true - If the element is successfully inserted/update.	 
	 * @exception throw BTreeException if failed to insert with the cause Embedded. 
	 */
	@Override
	public boolean insert(K key, E entity) {
		try {
			if(key == null || entity == null) {
				throw new BTreeException("Key/Entity cannot be null");
			}
			return this.insert.insert(new Tuple<K,E>(key, entity));	
		} catch(IOException io) {
			throw new BTreeException("Failed to insert",io);
		}		
	}
	
	/**
	 * @retrun Entity If the element is found 
	 * null If the key is not found
	 * @exception throw BTreeException in case of failure with the cause Embedded. 
	 */
	@Override
	public E search(K key) {
		try {
			TupleMetadata<K, E> metadata = this.search.search(key);
			if(metadata == null) {
				return null;
			}
			return metadata.node.tuples[metadata.pos].e;
		} catch(Exception e) {
			throw new BTreeException("Failed to Search",e);
		}		
	}

	@Override
	public boolean delete(K key) {		
		try {
			if(key == null) {
				return false;
			}
			if(rootNode.keyTally == 0) {
				return false;
			}
			
			this.delete.delete(key);	
			return true;
		} catch(Exception e) {
			throw new BTreeException("Failed to delete",e);
		}
	}

	@Override
	public boolean init() {
		//Load or allocate the node
		initRootNode();		

		this.insert = Insert.getNewInstance(this);
		this.search = Search.getNewInstance(this);
		this.successor = Successor.getNewInstance(this);
		this.delete = Delete.getNewInstance(this);
		return true;
	}

	/**
	 * returns
	 * @exception 
	 */
	@Override
	public boolean close() {
		try {
			//close the resources
			ILogger.LOG.info("Closing the BTree resources");
			fileChannel.close();
			accessFile.close();
		} catch (IOException e) {

		}
		return true;
	}

	@Override
	protected void finalize() throws Throwable {		
		try {
			//close the resources
			ILogger.LOG.info("Invoked by GC. Closing the BTree resources.");
			fileChannel.close();
			accessFile.close();
		} catch (IOException e) {

		}
		super.finalize();
	}

	/**
	 * @return successor if exists else null 
	 */
	@Override
	public K succecessor(K k) {
		try {
			TupleMetadata<K, E> tupleMetadata = this.successor.get(k);
			K successor = tupleMetadata.node.tuples[tupleMetadata.pos].key;
			ILogger.info("successor for "+k+" is "+successor);
			if(successor.compareTo(k) == 0) {
				return null;
			}
			return successor;
		} catch(IOException io) {
			throw new BTreeException("Failed to insert",io);
		}		
	}
	
	public boolean isValid() {
		return new Stats<K,E>(this).isValid();
	}

	/**
	 * TODO
	 */
	@Override
	public K predecessor(K k) {		
		return null;
	}
}
