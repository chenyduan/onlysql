# OnlySql
使用JPA设计，
- 生成 QEntity 用于SQL DSL ;
- 生成 REntity 对应数据库表字段
- 生成 TEntity 记录表信息： tableName,fields 



## 生成类示例

### TEntity
生成TEntity，记录表信息
~~~
public final class TSysUser{
    // 表字段
    public static final class Table {
        public static final String tableName = "sys_user";
        public static final String _id = "id";
        public static final String _createTime = "create_time";
        public static final String _age = "age";
    }
    // 生成字段信息
    public final TSuperEntity _super= new TSuperEntity();

    public final DateTimePath createTime = _super.createTime;

    public final DateTimePath updateTime = new DateTimePath("updateTime", LocalDateTime.class);

    public final NumberPath<Long> createBy = new NumberPath("createBy", Long.class);

    public final NumberPath<Long> updateBy = new NumberPath("updateBy", Long.class);

    public final NumberPath<Integer> revision = new NumberPath("revision", Integer.class);
  
  
    public final NumberPath<Integer> age = new NumberPath("age", Integer.class);

}

~~~

### REntity 
将当前Entity和SuperEntity的字段统一归纳到当前实体的REntity
~~~

public record RSysUser(Long id,LocalDateTime createTime){

}
~~~

### QEntity 
组装DSL，其中 @MappedSuperclass 不生成对应QEntity
~~~
public class QSysUser  {
 

  
  public Select select(){
    return new Select(TSysUser.table);
  }
  
    public Insert insert(){
    return new Insert();
  }
  
  public Update update(){
    return new Update();
  }
  
  public Delete delete(){
    return new Delete();
  }
    
}
~~~


###  DSL
~~~
# insert
new QSysUser().insert().id(1L).name('admin').exe();
# update
new QSysUser().update().name('admin').where().id(1L).exe();
# delete
new QSysUser().delete().where().id(1L).exe();

# select
new QSysUser().select().where().id(1L).exe();

new QSysUser().select(QSysUser.name,QSysUser.id).where().id(1L).exe();

~~~



## JPA 设计
推荐使用组合而非继承，OnlySQL 在编译生成TEntity时会抹平字段；