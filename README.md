# MyDict Spring Boot Starter

🚀 **全新升级版本** - 支持JDK21+ 和 Spring Boot 3.x，真正的零配置使用！

自定义数据字典，编译期间自动生成字典字段，就像Lombok一样简单易用。

## ✨ 功能特性

- 🎯 **零配置** - 无需任何IDEA设置或复杂配置
- ⚡ **编译时处理** - 编译期自动生成字典描述字段
- 🔧 **Spring Boot 3** - 全面支持最新版本
- 🌟 **JDK21+** - 拥抱现代Java生态
- 🔗 **MyBatis-Plus集成** - 自动添加`@TableField(exist = false)`
- 🎨 **自定义注解** - 支持在生成字段上添加任意注解
- 💾 **Caffeine缓存** - 自动缓存字典查询结果，可配置TTL和容量
- 🔤 **智能命名** - 自动识别蛇形/驼峰命名，支持camelCase开关

## 📋 版本兼容性

| 版本 | JDK要求 | Spring Boot | 配置要求 |
|------|---------|-------------|----------|
| 1.0-jdk21 | **JDK21-24+** | 3.0+ | Maven 编译器配置(一次性) |
| 1.2 | JDK8+ | 2.x | 需要IDEA配置 |

> **⚠️ JDK 24 用户注意**:
> - 如果遇到注解处理器不生成字段的问题,请确保使用 **JDK 24 编译的最新版本**
> - 本项目已支持 JDK 21-24+,但需要用最高 JDK 版本编译以确保向下兼容

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
        <version>1.0-jdk21</version>
    </dependency>
</dependencies>
```

### 2. ⚠️ 配置Maven编译器（必需！）

由于JDK 21+的模块化限制，注解处理器需要访问javac内部API。**必须**在项目的`pom.xml`中添加以下配置：

#### JDK 21-23 配置

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

#### ⚠️ JDK 24+ 额外配置

**JDK 24 引入了更严格的模块访问控制**，除了上述 `--add-exports` 参数外，还需要添加 `--add-opens` 参数以支持深度反射：

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <fork>true</fork>
                <compilerArgs>
                    <!-- JDK 21-23: Export javac internal APIs -->
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
                    <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>

                    <!-- JDK 24+: Additional opens required for deep reflection -->
                    <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
                    <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
                    <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
                    <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
                    <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
                    <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

> **💡 JDK 版本差异说明：**
>
> - **JDK 21-23**: 只需 `--add-exports` 即可正常使用
> - **JDK 24+**: 额外需要 `--add-opens` 以支持注解处理器的深度反射访问
> - **建议**: 如果你的项目可能在不同JDK版本间切换，建议直接使用JDK 24+的完整配置（向下兼容）

> **💡 为什么需要这个配置？**
>
> 本项目使用javac的Tree API直接修改抽象语法树（AST），以在编译期生成代码。这与Lombok的工作原理类似，但JDK 21+引入了强模块化系统，这些API默认不对外开放。
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

## 💾 缓存配置

MyDict 内置 Caffeine 缓存以提升字典查询性能。默认启用，可通过配置调整：

### application.yml 配置

```yaml
mydict:
  cache:
    enabled: true        # 是否启用缓存（默认：true）
    ttl: 30             # 缓存过期时间，单位秒（默认：30秒，适用于同一请求内多层嵌套POJO场景）
    max-size: 10000     # 最大缓存条目数（默认：10000）
    record-stats: false # 是否记录缓存统计（默认：false）
```

### 缓存管理

```java
// 清空所有缓存（例如字典数据更新后）
MyDictHelper.clearCache();

// 清除指定字典的缓存
MyDictHelper.clearCache("user_status");

// 获取缓存统计信息（需要 recordStats=true）
CacheStats stats = MyDictHelper.getCacheStats();
System.out.println("缓存命中率: " + stats.hitRate());
```

### 何时使用缓存

✅ **推荐启用**：
- 字典数据相对稳定
- 字典查询频繁（如列表页面）
- 字典数据源性能有限（如远程数据库）

⚠️ **建议禁用**：
- 字典数据频繁变更
- 实时性要求极高
- 内存资源紧张

## 🔤 字段命名策略

MyDict 支持智能命名识别和 `camelCase` 开关控制生成字段的命名风格。

### camelCase 参数

```java
@MyDict(name = "status", camelCase = true)   // 生成：statusDesc
private String status;

@MyDict(name = "type", camelCase = false)    // 生成：type_desc
private String type;
```

### 智能命名规则（优先级从高到低）

| 原字段名 | camelCase | 生成字段名 | 说明 |
|---------|-----------|-----------|------|
| `user_status` | true/false | `user_status_desc` | ①包含下划线，忽略开关，生成蛇形 |
| `userName` | true/false | `userNameDesc` | ②大小写混合，忽略开关，生成驼峰 |
| `type` | **true** | `typeDesc` | ③全小写，遵循开关，生成驼峰 |
| `type` | **false** | `type_desc` | ③全小写，遵循开关，生成蛇形 |
| `SEX_TYPE` | true/false | `SEX_TYPE_DESC` | ①全大写蛇形 |

### 推荐实践

- **统一团队规范**：根据项目命名规范设置 `camelCase`
- **保持一致性**：同一实体类建议使用相同的 `camelCase` 设置
- **依赖智能识别**：如果字段名已有明确风格（含下划线或驼峰），无需手动设置

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

| 特性 | 旧版本(1.2) | 新版本(1.0-jdk21) |
|------|------------|-----------------|
| IDEA配置 | ❌ 需要手动配置VM参数 | ✅ 无需IDEA配置 |
| Maven配置 | 复杂 | 简单（一次性配置） |
| JDK版本 | JDK8+ | JDK21+ |
| Spring Boot | 2.x | 3.x |
| 依赖管理 | 需要tools.jar | 现代化依赖 |
| 模块系统 | 不支持 | 完全支持 |
| 缓存支持 | ❌ 无 | ✅ Caffeine缓存 |
| 智能命名 | ❌ 无 | ✅ 自动识别+开关 |

## ❗ 重要说明

### 配置要求

**⚠️ 必需的 Maven 配置：**
- ✅ **一次性配置**: 在`pom.xml`中添加编译器参数（见上方步骤2）
- ✅ **原因说明**: JDK 21+模块化系统的安全限制
- ✅ **配置简单**: 直接复制粘贴即可
- ✅ **适用范围**: 命令行编译（`mvn compile`）

**✅ IDEA 增量编译支持（新功能）：**

从最新版本开始，MyDict 已原生支持 IntelliJ IDEA 的增量编译环境：

- ✅ **自动检测 IDEA 环境**：无需手动配置 VM 参数
- ✅ **Proxy 自动解包**：内部处理 IDEA 的 ProcessingEnvironment Proxy
- ✅ **零额外配置**：与命令行编译体验完全一致

> **🎉 技术说明**：
>
> IDEA 的增量编译会将 `ProcessingEnvironment` 包装成 Proxy，导致无法直接访问 javac 内部 API。
> 我们使用 JetBrains 官方的 `org.jetbrains.jps.javac.APIWrappers.unwrap()` 方法自动解包，
> 实现了与 IDEA 增量编译的完美兼容。
>
> 参考：[MapStruct #2215](https://github.com/mapstruct/mapstruct/issues/2215), [javalin-openapi #141](https://github.com/javalin/javalin-openapi/issues/141)

**⚠️ 如果仍遇到问题（极少数情况）：**

在旧版本 IDEA 或特殊环境下，如果遇到编译问题，可以尝试以下方案之一：

**方案 1**：配置 IDEA VM 参数
1. `Settings` → `Build, Execution, Deployment` → `Compiler` → `Shared build process VM options`
2. 添加：`-Djps.track.ap.dependencies=false`
3. 重启 IDEA

**方案 2**：委托给 Maven（推荐）
1. `Settings` → `Build Tools` → `Maven` → `Runner`
2. 勾选：`Delegate IDE build/run actions to Maven`

### 为什么不是真正的"零配置"？

由于本项目采用与Lombok类似的AST修改技术，需要访问javac编译器的内部API。JDK 21+引入了强模块化系统（JPMS），这些API默认不对外开放。

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

#### 1.5 JDK 24 编译成功但字段没有生成

**现象**：
- 使用 JDK 24 编译,没有错误
- 但是 `xxxDesc` 字段和 getter/setter 方法没有生成

**原因**：
- MyDict 注解处理器 JAR 是用较低版本 JDK 编译的
- JDK 24 的模块系统更严格,导致运行时兼容性问题

**解决方案**：

1. **方案 1**: 切换到 JDK 21-23 使用(临时方案)
   ```bash
   export JAVA_HOME=/path/to/jdk21
   mvn clean compile
   ```

2. **方案 2**: 使用 JDK 24 编译的最新版本(推荐)
   - 确保使用最新发布的 mydict 版本
   - 或者自行用 JDK 24 编译本项目:
     ```bash
     git clone https://github.com/mocanjie/mydict-spring-boot-starter
     cd mydict-spring-boot-starter
     # 确保使用 JDK 24
     mvn clean install -DskipTests
     ```

3. **验证是否生成**：
   ```bash
   javap -p target/classes/your/package/YourClass.class
   # 应该能看到 xxxDesc 字段和对应的 getter/setter
   ```

---

#### 2. 生成的字段在 IDE 中看不到（显示红色波浪线）

**这是正常现象！** 与 Lombok 一样，字段是在编译期生成的，IDE 在编辑时看不到。

**解决**：
- ✅ 编译后字段会存在于 `.class` 文件中
- ✅ 运行时完全正常，可以正常访问 `obj.getXxxDesc()`
- ✅ 如果需要 IDE 支持，未来会提供 IDEA 插件

**临时方案**（如果红线让你不舒服）：
- 在 IDE 中使用 Maven 编译（`mvn compile`）
- 或委托给 Maven：`Settings` → `Build Tools` → `Maven` → `Runner` → 勾选 `Delegate IDE build/run actions to Maven`

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
        <java.version>21</java.version>
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
            <version>1.0-jdk21</version>
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

            <!-- ⚠️ 必需配置：支持 JDK 21-24+ -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <fork>true</fork>
                    <compilerArgs>
                        <!-- JDK 21-23: Export javac internal APIs -->
                        <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
                        <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
                        <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
                        <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
                        <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
                        <arg>-J--add-exports=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>

                        <!-- JDK 24+: Additional opens required -->
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED</arg>
                        <arg>-J--add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED</arg>
                    </compilerArgs>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## 🔨 从源码编译

### 编译要求

- **推荐**: 使用 **JDK 24** 编译,以获得最佳跨版本兼容性
- **最低**: JDK 21+

### 编译步骤

```bash
# 1. 克隆项目
git clone https://github.com/mocanjie/mydict-spring-boot-starter.git
cd mydict-spring-boot-starter

# 2. 确保使用 JDK 24 (推荐) 或 JDK 21+
java -version

# 3. 编译并安装到本地 Maven 仓库
mvn clean install -DskipTests

# 4. 在你的项目中使用
# 确保 pom.xml 中的版本号与编译的版本一致
```

### 为什么推荐用 JDK 24 编译?

1. **向下兼容**: JDK 24 编译的 JAR 可以在 JDK 21+ 上运行
2. **模块系统**: JDK 24 的模块系统最严格,确保兼容性
3. **未来保障**: 为未来的 JDK 版本做好准备

> **⚠️ 注意**: 如果用 JDK 21 编译,在 JDK 24 环境可能遇到注解处理器不工作的问题。

## 📝 更新日志

### 1.0-jdk21 版本 (2025)
- ✅ 支持 JDK 21-24+ 和 Spring Boot 3.x
- ✅ 完整兼容 JDK 24 的严格模块系统
- ✅ 移除对 tools.jar 的依赖
- ✅ 现代化模块系统支持
- ✅ 优化 MyBatis-Plus 集成
- ✅ 改进错误处理和兼容性
- ✅ 集成 Caffeine 3.2.3 缓存
- ✅ 支持 camelCase 命名开关
- ✅ 智能命名识别（蛇形/驼峰）
- ✅ **原生支持 IDEA 增量编译**（自动解包 Proxy）
- ✅ **JDK 24 全面兼容**（需用 JDK 24 编译）

### 1.2 版本 (历史版本)
- 支持JDK8和Spring Boot 2.x
- 需要手动IDEA配置

---

💡 **享受零配置的开发体验吧！** 就像使用Lombok一样简单优雅。

