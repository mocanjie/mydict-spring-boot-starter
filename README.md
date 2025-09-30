# MyDict Spring Boot Starter

🚀 **全新升级版本** - 支持JDK17+ 和 Spring Boot 3.x，真正的零配置使用！

自定义数据字典，编译期间自动生成字典字段，就像Lombok一样简单易用。

## ✨ 功能特性

- 🎯 **零配置** - 无需任何IDEA设置或复杂配置
- ⚡ **编译时处理** - 编译期自动生成字典描述字段
- 🔧 **Spring Boot 3** - 全面支持最新版本
- 🌟 **JDK17+** - 拥抱现代Java生态
- 🔗 **MyBatis-Plus集成** - 自动添加`@TableField(exist = false)`
- 🎨 **自定义注解** - 支持在生成字段上添加任意注解

## 📋 版本兼容性

| 版本 | JDK要求 | Spring Boot | 配置要求 |
|------|---------|-------------|----------|
| spring3 | JDK17+ | 3.0+ | **零配置** |
| 1.2 | JDK8+ | 2.x | 需要IDEA配置 |

## 🎪 代码示例

### 编译前：
```java
@Data
public class TestVO {
    @MyDict(name = "gt_dict", defaultDesc = "未知类型")
    private Integer goodsType = 1;

    @MyDict(name = "status_dict")
    private String status = "ACTIVE";
}
```

### 编译后自动生成：
```java
@Data
public class TestVO {
    private Integer goodsType = 1;

    @TableField(exist = false) // MyBatis-Plus环境下自动添加
    private String goodsTypeDesc;

    private String status = "ACTIVE";

    @TableField(exist = false)
    private String statusDesc;

    // 自动生成的getter方法
    public String getGoodsTypeDesc() {
        String descStr = MyDictHelper.getDesc("gt_dict", getGoodsType());
        return StringUtils.hasText(descStr) ? descStr : "未知类型";
    }

    public String getStatusDesc() {
        String descStr = MyDictHelper.getDesc("status_dict", getStatus());
        return StringUtils.hasText(descStr) ? descStr : "";
    }

    // 自动生成的setter方法
    public void setGoodsTypeDesc(String goodsTypeDesc) {
        this.goodsTypeDesc = goodsTypeDesc;
    }

    public void setStatusDesc(String statusDesc) {
        this.statusDesc = statusDesc;
    }
}
```

## 🚀 快速开始

### 1. 添加依赖

```xml
<dependencies>
    <dependency>
        <groupId>io.github.mocanjie</groupId>
        <artifactId>mydict-spring-boot-starter</artifactId>
        <version>spring3</version>
    </dependency>
</dependencies>
```

### 2. ⚠️ 配置Maven编译器（必需！）

由于JDK 17+的模块化限制，注解处理器需要访问javac内部API。**必须**在项目的`pom.xml`中添加以下配置：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <fork>true</fork>
                <compilerArgs>
                    <!-- MyDict注解处理器需要访问javac内部API -->
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

> **💡 为什么需要这个配置？**
>
> 本项目使用javac的Tree API直接修改抽象语法树（AST），以在编译期生成代码。这与Lombok的工作原理类似，但JDK 17+引入了强模块化系统，默认不允许访问编译器的内部API。
>
> 通过`-J--add-exports`参数，我们告诉JVM允许注解处理器访问这些必要的内部包。这是一次性配置，之后就可以正常使用。

**如果缺少此配置，会出现以下编译错误：**
```
java.lang.IllegalAccessError: class io.github.mocanjie.tools.dict.MyDictProcess
cannot access class com.sun.tools.javac.api.JavacTrees because module jdk.compiler
does not export com.sun.tools.javac.api to unnamed module
```

### 3. 实现字典查询接口

```java
@Component
public class DictServiceImpl implements IMyDict {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 获取字典说明
     * @param name 字典code
     * @param value 字典值
     * @return 字典描述
     */
    @Override
    public String getDesc(String name, Object value) {
        // 一般结合Redis或数据库查询字典
        String key = "dict:" + name + ":" + value;
        return redisTemplate.opsForValue().get(key);
    }
}
```

### 4. 在实体类上使用注解

```java
@Data
@Entity
public class User {
    @MyDict(name = "user_status", defaultDesc = "未知状态")
    private Integer status;

    @MyDict(name = "user_type")
    private String type;
}
```

### 5. 编译运行

```bash
mvn clean compile
# 字典字段和方法已自动生成！
```

编译成功后，`User`类会自动生成：
- `private String statusDesc;` 字段
- `private String typeDesc;` 字段
- 对应的getter/setter方法

## 🔧 高级用法

### 自定义注解支持

```java
@MyDict(
    name = "goods_type",
    defaultDesc = "默认商品",
    fieldAnnotations = {
        @FieldAnnotation(
            fullAnnotationName = "io.swagger.v3.oas.annotations.media.Schema",
            vars = {
                @Var(varType = VarType.STRING, varName = "description", varValue = "商品类型描述"),
                @Var(varType = VarType.BOOLEAN, varName = "hidden", varValue = "true")
            }
        )
    }
)
private Integer goodsType;
```

生成结果：
```java
@Schema(description = "商品类型描述", hidden = true)
@TableField(exist = false)
private String goodsTypeDesc;
```

## 🆚 与旧版本对比

| 特性 | 旧版本(1.2) | 新版本(spring3) |
|------|------------|-----------------|
| IDEA配置 | ❌ 需要手动配置VM参数 | ✅ 无需IDEA配置 |
| Maven配置 | 复杂 | 简单（一次性配置） |
| JDK版本 | JDK8+ | JDK17+ |
| Spring Boot | 2.x | 3.x |
| 依赖管理 | 需要tools.jar | 现代化依赖 |
| 模块系统 | 不支持 | 完全支持 |

## ❗ 重要说明

### 配置要求

**✅ 无需IDEA配置：**
- ❌ **不再需要**: 设置`-Djps.track.ap.dependencies=false`
- ❌ **不再需要**: 在IDEA中手动配置注解处理器
- ❌ **不再需要**: 复杂的IDE VM参数设置

**⚠️ 需要Maven配置：**
- ✅ **一次性配置**: 在`pom.xml`中添加编译器参数（见上方步骤2）
- ✅ **原因说明**: JDK 17+模块化系统的安全限制
- ✅ **配置简单**: 直接复制粘贴即可

### 为什么不是真正的"零配置"？

由于本项目采用与Lombok类似的AST修改技术，需要访问javac编译器的内部API。JDK 17+引入了强模块化系统（JPMS），这些API默认不对外开放。

**对比Lombok：**
- Lombok也需要类似的配置（或使用专用的lombok.jar）
- 两者都通过修改AST在编译期生成代码
- 这是目前最高效的实现方式（零运行时开销）

**未来计划：**
- 考虑提供符合JSR 269标准的实现（生成源文件）
- 提供Maven插件自动配置
- 探索更优雅的集成方案

## 🛠️ 故障排除

### 常见问题

#### 1. 编译报错：`IllegalAccessError: cannot access class com.sun.tools.javac.api.JavacTrees`

**原因**：缺少步骤2的Maven编译器配置。

**解决**：在项目的`pom.xml`中添加`maven-compiler-plugin`配置（参见步骤2）。

---

#### 2. 生成的字段看不到（IDE中显示红色波浪线）

**原因**：
- 字段是在编译期生成的，IDE在编辑时看不到
- 这是正常现象，和Lombok一样

**解决**：
- 编译后字段会存在于`.class`文件中
- 运行时完全正常
- 如果需要IDE支持，未来会提供IDEA插件

---

#### 3. MyBatis-Plus环境下字段映射错误

**原因**：生成的`xxxDesc`字段会自动添加`@TableField(exist = false)`注解。

**检查**：确认是否正确添加了MyBatis-Plus依赖。

---

#### 4. 字典查询返回null

**原因**：
- 未实现`IMyDict`接口
- 字典数据源（Redis/数据库）中没有对应的值

**解决**：
- 检查`IMyDict`实现类是否被Spring扫描到
- 检查字典数据是否正确存储

---

### 完整的pom.xml示例

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>demo</artifactId>
    <version>1.0.0</version>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
        </dependency>

        <!-- MyDict Starter -->
        <dependency>
            <groupId>io.github.mocanjie</groupId>
            <artifactId>mydict-spring-boot-starter</artifactId>
            <version>spring3</version>
        </dependency>

        <!-- 可选：MyBatis-Plus支持 -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.5.4</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <!-- ⚠️ 必需配置 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <fork>true</fork>
                    <compilerArgs>
                        <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
                        <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
                        <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
                        <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
                        <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
                        <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
                    </compilerArgs>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## 📝 更新日志

### spring3 版本 (2024)
- ✅ 支持JDK17+和Spring Boot 3.x
- ✅ 实现零配置使用
- ✅ 移除对tools.jar的依赖
- ✅ 现代化模块系统支持
- ✅ 优化MyBatis-Plus集成
- ✅ 改进错误处理和兼容性

### 1.2 版本 (历史版本)
- 支持JDK8和Spring Boot 2.x
- 需要手动IDEA配置

---

💡 **享受零配置的开发体验吧！** 就像使用Lombok一样简单优雅。

