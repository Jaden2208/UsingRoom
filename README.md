# UsingRoom
Room을 사용한 정말정말 간단한 메모장

## Demo
Try demo on [here](https://appetize.io/app/tfy4k9rbup6bt7e5ba6wd84u2r?device=nexus5&scale=75&orientation=portrait&osVersion=8.1)!

## 기능
1. 하나의 Text View에 계속 추가해주는 방식
2. 그게 전부. 수정, 삭제 기능 구현 x

## 코드 설명
- 할 일 테이블(Entity)를 구성하는 애트리뷰트들을 담은 data class Todo (title, id로 구성)를 만든다. 여기에는 반드시 이 클래스가 엔터티 임을 표현해주는 어노테이션 `@Entity`를 추가해야 한다.

  이때 id를 PK로 지정해주며, 자동으로 생성되도록하기 위해,
  ```kotlin
  @PrimaryKey(autoGenerate = true)
  var id: Int = 0 
  ```
  위와 같이 구현했다.

- Dao (Database Access Object) 인터페이스 구현
  여기에는 반드시 이 클래스가 Data Access Object 임을 표현해주는 어노테이션 `@Dao`를 추가해야 한다.

  쿼리문을 실행하기 위해서 `getAll()`을 호출하도록 구현했다.
  ```kotlin
  @Query("select * from Todo")
  fun getAll(): List<Todo>
  ``` 

- insert, update, delete에 대한 구현은 아래와 같이 간단하게 구현할 수 있다.
  ```kotlin
  @Insert
  fun insert(todo: Todo)
  
  @Update
  fun update(todo: Todo)
  
  @Delete
  fun delete(todo: Todo)
  ```

- 메인엑티비티에서 AppDatabase를 생성한 뒤 todoDao()를 통해 Todo를 조작한다.
  
  앱 데이터베이스 클래스에는 반드시 이 클래스가 Database 임을 표현해주는 어노테이션 `@Database`를 추가해야 한다
  ```kotlin
  // AppDatabase.kt
  
  @Database(entities = [Todo::class], version = 1)
  abstract class AppDatabase : RoomDatabase() {
      abstract fun todoDao(): TodoDao
  }
  ```

- 메인엑티비티에서 AppDatabase를 생성하는 방법.
  ```kotlin
          val db = Room.databaseBuilder(
              applicationContext,
              AppDatabase::class.java, "todo-db"
          ).allowMainThreadQueries().build()
  ```
  DB는 백그라운드에서 동작하지 않으면 에러가 난다. 하지만 여기서는 간단하게 `allowMainThreadQueries()`를 이용해 에러 없이 동작하도록 했다.

#### 참조
- [안드로이드 생존코딩 : 모던 안드로이드 - DB를 이용한 데이터 저장 방법 Room](https://www.youtube.com/watch?v=97xmJRZRGm4&list=PLxTmPHxRH3VXHOBnaGQcbSGslbAjr8obc&index=2)

- [**Room 사용법에 대한 더 상세한 내용**](https://medium.com/@gus0000123/mvvm-aac-room-%EC%82%AC%EC%9A%A9%EB%B2%95-2-%EC%82%AC%EC%9A%A9%ED%8E%B8-43ea8a936b12)

