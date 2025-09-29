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

### 1. 添加依赖（零配置！）

```xml
<dependencies>
    <dependency>
        <groupId>io.github.mocanjie</groupId>
        <artifactId>mydict-spring-boot-starter</artifactId>
        <version>spring3</version>
    </dependency>
</dependencies>
```

### 2. 实现字典查询接口

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

### 3. 在实体类上使用注解

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

### 4. 编译运行

```bash
mvn clean compile
# 字典字段和方法已自动生成！
```

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
| IDEA配置 | ❌ 需要手动配置VM参数 | ✅ 零配置 |
| JDK版本 | JDK8+ | JDK17+ |
| Spring Boot | 2.x | 3.x |
| 依赖管理 | 需要tools.jar | 现代化依赖 |
| 模块系统 | 不支持 | 完全支持 |

## ❗ 重要说明

**🎉 无需任何IDEA配置！**

- ❌ **不再需要**: 设置`-Djps.track.ap.dependencies=false`
- ❌ **不再需要**: 手动配置注解处理器
- ❌ **不再需要**: 复杂的VM参数设置

只需添加Maven依赖，即可像使用Lombok一样简单！

## 🛠️ 故障排除

### Maven 配置建议

如果遇到编译问题，确保你的Maven配置支持JDK17+：

```xml
<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
        </plugin>
    </plugins>
</build>
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

