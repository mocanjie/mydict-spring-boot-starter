# mydict
自定义数据字典，编译期间自动生成字典字段。

### 编译前：

```java
@Data
public class TestVO {
    @MyDict(name = "gt_dict")
    private Integer goodsType = 1;
}
```

### 编译后：

```java
public class TestVO {
    private Integer goodsType = 1;
    private String goodsTypeDesc;
```

## 使用方法

### 1、配置项目pom.xml

```xml
<dependency>
    <groupId>io.github.mocanjie</groupId>
    <artifactId>mydict-spring-boot-starter</artifactId>
    <version>1.0</version>
</dependency>
```


### 2、实现字典接口 IMyDict的getDesc方法

```java
@Component
public class DictImpl implements IMyDict {
    
   /**
     * 获取字典说明
     * @param name 字典code
     * @param value 字典值
     * @return
     */
    @Override
    public String getDesc(String name, Object value) {
        return "一般结合redis来获取字典说明";
    }
}
```


## IDEA开发环境下还需要如下设置

#### 配置项 ： File 》 Settings 》 Build，Execution，Deployment 》 Complier 》Shared build process VM options  

####  配置值：  -Djps.track.ap.dependencies=false

