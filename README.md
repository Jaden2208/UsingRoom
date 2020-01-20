# UsingRoom
**Room, LiveData, Coroutine, ViewModel, DataBinding을 사용한 정말정말 간단한 메모장**

- [Room](#room)
- [LiveData](#livedata)
- [Coroutine](#coroutine)
- [ViewModel](#viewmodel)
- [DataBinding](#databinding)

## Demo

Try demo on [here](https://appetize.io/app/1c9m363qd921v36tq5d61gjrym?device=nexus5&scale=75&orientation=portrait&osVersion=8.1)!

## 기능

1. 하나의 Text View에 계속 추가해주는 방식
2. 그게 전부. 수정, 삭제 기능 구현 x

---

## Room

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

- [**Room 사용법에 대한 더 상세한 내용**](https://medium.com/@gus0000123/mvvm-aac-room-%EC%82%AC%EC%9A%A9%EB%B2%95-2-%EC%82%AC%EC%9A%A9%ED%8E%B8-43ea8a936b12)

---

## LiveData

> 어떤 데이터를 불러오기 위해서 그 때마다 무언가를 호출해줘야하는 번거로움을 없앨 수 있다.
> LiveData는 관찰(Observing)을 통해 자동으로 갱신되도록 해준다.

예를 들어서 우리가 관찰하고 싶은게 Todo 라는 테이블이라고 하면, 관찰하고자 하는 것을 아래와 같이 `LiveData<>`로 감싸주면 된다.

```kotlin
// LiveData 적용 전:
// fun getAll(): List<Todo>
fun getAll(): LiveData<List<Todo>>
```

LiveData를 적용하기 전에는 변경된 데이터를 뷰에 적용해줘야 할 때 마다 일일이

```kotlin
txt_result.text = db.todoDao().getAll().toString()
```

이런 코드를 추가 해주어야했다.

하지만 LiveData를 적용함으로써

```kotlin
db.todoDao().getAll().observe(this, Observer {
    txt_result.text = it.toString()
})
```

이 코드 하나로 변경될 때 마다 알아서 뷰에도 적용되도록 구현할 수 있다.
`it`은 변경된 내용을 의미한다.

---

## Coroutine
이전에 단순하게 [Room]을 이용해서 로컬 데이터베이스를 생성할 때는,

```kotlin
// 앱 데이터베이스 생성
val db = Room.databaseBuilder(
    applicationContext,
    AppDatabase::class.java, "todo-db"
).allowMainThreadQueries().build()
```

위와 같이 `allowMainThreadQueries()` 를 이용해 메인 스레드에서도 동작하도록 구현했다.
하지만 메인 스레드에서 데이터베이스에 접근하지 못하게 되어있기 때문에 임시방편으로 구현한 것일 뿐이었다.
현재는 단순한 DB작업만을 요구하기 때문에 문제가 되지않지만, 빈번한 DB작업이 필요한 경우에는 앱의 성능을 떨어트리게 된다.

**따라서 DB를 다룰 때는 백그라운드 작업, 즉 비동기 처리가 필요한 것이다.**

자바의 경우는 AsyncTask를 사용하지만 코틀린의 경우에는 더 좋은 라이브러리가 존재한다.

바로 **코루틴** 이다.

- 예제 코드

  ```kotlin
  lifecycleScope.launch(Dispatchers.IO) {
      db.todoDao().insert(Todo(edit_todo.text.toString()))
  }
  ```

  `Dispatchers.IO` 는 백그라운드에서 실행하겠다는 의미라고 생각하면 된다.

코루틴을 적용했다면 이전에 DB생성코드에서 사용한 `allowMainThreadQueries()` 는 제거해도 된다.

#### 코루틴 추가 사항

어떤 메서드에 코루틴을 적용할 때 CoroutineScope안에 작성하지 않아도 코드 상의 오류는 보이지 않는다. 하지만 suspend라는 키워드를 코루틴을 적용해야 하는 메서드에 추가 하면 CoroutineScope 밖에 해당 메서드를 호출할 때 빨간줄, 즉 오류가 뜨도록 할 수 있다.

```kotlin
suspend fun insert(todo: Todo) {
    db.todoDao().insert(todo)
}
```

---

## ViewModel

- UI 와 로직을 분리하기 좋다.

- 기울임을 통한 화면 전환시에 새로 화면이 초기화되는데, ViewModel의 경우에는 Activity가 완전히 종료될 때 까지 하나의 lifecycle을 유지하기 때문에 ViewModel에 넣어둔 데이터들은 다시 초기화 되지 않는다.

코드를 작성할 때 하나의 Activity에 모든 기능들이 혼재 되어있는 것 보다는 별도의 클래스로 나눠주는게 좋다.

그 클래스는 AndroidViewModel를 상속받아야 하고, Application을 생성자에서 받도록 한다.

```kotlin
class MainViewModel(application: Application): AndroidViewModel(application) {
  ...
}
```

그리고 MainActivity 에서 ViewModel을 불러오기 위해서는 `ViewModelProviders`를 이용하면 된다.  

```kotlin
val viewModel = ViewModelProvides.of(this)[MainViewModel::class.java]
```

---

## DataBinding

데이터바인딩을 사용하면 xml에다가 데이터를 넘겨줄 수 있다.
Logic에 관한 부분이 현재 MainViewModel에 다 들어있는데 이것들을 XML에 던져버리고 그거에 반응해서 알아서 동작으로 하도록 수정할 것이다.

먼저 datBinding 설정을 위해 gradle 설정을 해준다.(자세한 건 맨 아래 참고 링크에서 확인)

그리고 데이터바인딩을 적용할 레이아웃을 layout으로 감싸준다.

```xml
<layout>
  ...
</layout>
```

기존에 `setContentView(R.layout.activity_main)` 하던 것을 아래 코드와 같이 수정해서 binding 객체를 저장해놓는다.

```kotlin
val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
```

그리고 livedata를 활용하기 위해서 다음의 코드를 추가한다.

```kotlin
binding.lifecycleOwner = this
```

이게 없으면 livedata를 관찰할 때 마다 데이터 xml이 refresh되지 않는다.

그리고 xml 파일에서 데이터를 받기 위해 `<data>`를 추가해준다.

```xml
<data>
    <variable
        name="viewModel"
        type="com.whalez.usingroom.MainViewModel" />
</data>
```

여기에 `MainActivity`의 `viewModel` 객체를 넣어주기 위해 다음의 코드를 추가한다.

```kotlin
binding.viewModel = viewModel
```

MainViewModel을 다음과 같이 수정한다.

```kotlin
class MainViewModel(application: Application): AndroidViewModel(application) {
    // 앱 데이터베이스 생성
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "todo-db"
    ).build()

    var todos: LiveData<List<Todo>> // .. 1

    var newTodo: String? = null // .. 2

    init {
        todos = getAll()
    }

    fun getAll(): LiveData<List<Todo>> {
        return db.todoDao().getAll()
    }

    fun insert(todo: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.todoDao().insert(Todo(todo))
        }
    }
}
```

1번 주석은 아래와 같이 데이터 바인딩을 구현한다.

```xml
<TextView
    android:id="@+id/txt_result"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:text="@{viewModel.todos.toString()}" />
```

2번 주석은 아래와 같이 데이터 바인딩을 구현한다.

```xml
<EditText
    android:id="@+id/edit_todo"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_alignParentStart="true"
    android:layout_toStartOf="@id/btn_add"
    android:hint="할 일"
    android:text="@={viewModel.newTodo}" />
```

그리고 `MainActivity` 에서 더 이상 필요 없는 코드를 제거 하고 나면 이렇게 짧게 줄일 수 있다.

```kotlin
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        val viewModel = ViewModelProviders.of(this)[MainViewModel::class.java]
        binding.viewModel = viewModel
    }
}
```


---


#### 참조
- [안드로이드 생존코딩 : 모던 안드로이드 - DB를 이용한 데이터 저장 방법 Room](https://www.youtube.com/watch?v=97xmJRZRGm4&list=PLxTmPHxRH3VXHOBnaGQcbSGslbAjr8obc&index=2)
- [안드로이드 생존코딩 : 모던 안드로이드 - LiveData](https://www.youtube.com/watch?v=E1OWnq_6R_0&list=PLxTmPHxRH3VXHOBnaGQcbSGslbAjr8obc&index=4)
- [안드로이드 생존코딩 : 모던 안드로이드 - Room 비동기처리 Coroutine](https://www.youtube.com/watch?v=-iD1pXTrZj8&list=PLxTmPHxRH3VXHOBnaGQcbSGslbAjr8obc&index=6)
- [안드로이드 생존코딩 : 모던 안드로이드 - UI와 로직분리 ViewModel](https://www.youtube.com/watch?v=2mqt0j-a5xI&list=PLxTmPHxRH3VXHOBnaGQcbSGslbAjr8obc&index=8)
- [안드로이드 생존코딩 : 모던 안드로이드 - DataBinding](https://www.youtube.com/watch?v=5BUGO9YnDz8&list=PLxTmPHxRH3VXHOBnaGQcbSGslbAjr8obc&index=10)
