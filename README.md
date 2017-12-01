Study and learn how indexes work in databases. Written in Java
==============================================================

The primary objective of study-indexdb is to understand how indexes work with some real usecases, which I believe gives better grasp of these concepts. One can try out or extend my code in their test/school projects to use and dig deeper into the indexes.

As I am myself beginer to these concepts, I will continue to evolve the project with the better design, optimizations, etc but will make sure implementation stay simple to understand. Any updates will be posted here.

This project currently has 3 algorithms:

[BTree - Disk based implementation](https://github.com/cduvvuri18/study-indexdb/tree/master/src/main/java/com/cduvvuri/sidb/persistent/btree)

[AVLTree - In memeory](https://github.com/cduvvuri18/study-indexdb/tree/master/src/main/java/com/cduvvuri/sidb/inmem/avltree)

[RedBlackTree - In memory](https://github.com/cduvvuri18/study-indexdb/tree/master/src/main/java/com/cduvvuri/sidb/inmem/redblack)


## Getting Started

**BTRee**

References: CLRS/Adam Drozdek/wiki. BTree is omnipresent :-).

***Create BTree***

While providing the folder path @ which index file is expected to create, do not provide the file name. Index file is created with the simple class name of the key(Looks weird right..I will fix this later).

Case - 1: Key and value both primitives
```
IndexFactory<Integer, String> factory = new BTreeFactory<Integer, String>();
DBIndex<Integer, String> index = factory.create(Integer.class, String.class, Paths.get("<absolute_path_do_not_mention_index_file_name>"));
```

Case - 2: Key and value both can be Java first class objects or just one. If the Key or Value is POJO. Have to follow below conventions. Refer the available annotations [Here](https://github.com/cduvvuri18/study-indexdb/tree/master/src/main/java/com/cduvvuri/sidb/annotations)



```
//If the Key is non-primitive, it has to be annotated with @Key and implement Comparable Interface
@Key
class StudentKey implements Comparable<StudentKey> {
	@Field(name = "id")
	private Integer id;

	@Field(name = "yob")
	private Integer yearOfBirth;

	StudentKey() {

	}

	StudentKey(Integer id, Integer yearOfBirth) {
		this.id = id;
		this.yearOfBirth = yearOfBirth;
	}

	@Override
	public int compareTo(StudentKey other) {
		int t = this.id.compareTo(other.id);
		if (t != 0) {
			return t;
		}

		return this.yearOfBirth.compareTo(other.yearOfBirth);
	}
}

//If the key is non-primitive, it has to be annotated with @Entity
@Entity
class Student {
	@TextField(name = "fName", length = 30)
	private String firstName;

	@TextField(name = "lName", length = 30)
	private String lastName;

	@Field(name = "rollNo")
	private Integer rollNo;
  
 	Student(String firstName, String lastName, Integer rollNo) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.rollNo = rollNo;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public Integer getRollNo() {
		return rollNo;
	}
}

//create the Index
IndexFactory<StudentKey, Student> idxFactory = new BTreeFactory<StudentKey, Student>();
DBIndex<StudentKey, Student> index = idxFactory.create(StudentKey.class, Student.class, Paths.get("<absolute_path_do_not_mention_index_file_name>"));
```

***insert, search, delete, successor***
```
//After create btree step, invoke init
index.init();

index.insert(new StudentKey(1, 1990), new Student("Chaitanya", "Duvvuri", 210210))

//continue with all your operations

//insert
//search

....

//finally

index.close();
```
All the operations performed above are persistent on disk. 

***Open BTree.***

While mentioning the path, do not provide the file name. BTree will look up for index file with the simple class name of the key provided in the given path(Looks weird right..I will fix this later).

```
index = idxFactory.open(Integer.class, String.class, Paths.get("<absolute_path_do_not_mention_index_file_name>"));

//search

//continue with all your operations

//finally
index.close();
```
*Refer the test cases for more details*


*None of these algorithm implementations are thread safe*

In Progress..


