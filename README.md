# pf4j-demo

写代码我们都知道要抽象，要封装变化，要实现开闭原则，比如对于很多相似的功能，我们可以将通用的功能抽象出来，然后把变化的不同的地方提取出去，比如模版模式、策略模式等都是实现类似的效果

比如对于策略模式，我们通常是定义一个接口，然后有不同的实现，这种是可以的，但是如果通用流程中要扩展的点较多的话，这些不同的实现也需要管理，可以把他们合并到一个单独的包中，再进一步，我们甚至可以将包单独提取出来，支持运行时加载包实现新增功能的支持

JDK对此功能的支持就是 SPI，但是它的限制较多，也不够灵活，比如dubbo就是自己定义了一套SPI的实现，这次我们来看另一个实现，[pf4j](https://github.com/pf4j/pf4j) 提供一套在基本框架中定义扩展点接口，然后通过不同的插件来实现扩展点的功能，来支持对新增开放对修改关闭

这次我们就看一下它的使用

## 使用

比如我们有一套通用的流程，假设是下单流程，不同的业务线等对于下单都有一些特殊点，但是它们的基本流程是相似的，这时候我们就可以先定义好通用的流程，不同的地方预留出扩展点接口，使用 pf4j 的流程如下

1. 先定义好扩展点接口（需要定义单独的包，因为基本应用和各个扩展点的包都依赖它）
2. 定义单独的插件包，其中实现扩展点接口的功能
3. 在应用中编写基本流程和扩展点的发现使用功能

这次我们就参考 pf4j 提供的例子来看一下

### 1. 定义扩展点接口

pom.xml首先声明依赖

```xml
<dependency>
    <groupId>org.pf4j</groupId>
    <artifactId>pf4j</artifactId>
    <version>3.6.0</version>
    <!-- 一般应用中会依赖这个包，所以这里设置为provided即可 -->
    <scope>provided</scope>
</dependency>
```

之后即可声明各个扩展点接口

```java
/**
 * 假设我们需要一个通知用户的功能
 * 需要注意的是，我们一定要继承 ExtensionPoint 接口，表示这是一个扩展点
 */
public interface Notice extends ExtensionPoint {
    boolean notice(List<Long> userIds);
}
```

### 2. 各个插件实现

pom.xml修改

```xml
<properties>
    <maven.compiler.source>8</maven.compiler.source>
    <maven.compiler.target>8</maven.compiler.target>

    <!-- 提供id和版本方便后续排查等使用 -->
    <plugin.id>email-plugin</plugin.id>
    <plugin.version>0.0.1</plugin.version>
    <!-- 插件类，如果不关系声明周期可以不提供 -->
    <plugin.class />
    <plugin.provider>user1</plugin.provider>
    <plugin.dependencies/>
</properties>

<dependencies>
    <!-- 相关依赖，注意 scope=provided -->
    <dependency>
        <groupId>org.pf4j</groupId>
        <artifactId>pf4j</artifactId>
        <version>3.6.0</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.zavier.demo</groupId>
        <artifactId>extension-api</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>

    <!-- 可以定义单独的依赖 -->
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>30.1.1-jre</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <!-- 配置打包插件，一个是要将所有依赖包打包成一起，避免和其他包冲突，第二是要将插件信息写入到manifest.mf -->
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <version>3.1.0</version>
            <configuration>
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
                <attach>false</attach>
                <archive>
                    <manifest>
                        <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
                    </manifest>
                    <manifestEntries>
                        <Plugin-Id>${plugin.id}</Plugin-Id>
                        <Plugin-Version>${plugin.version}</Plugin-Version>
                        <Plugin-Provider>${plugin.provider}</Plugin-Provider>
                        <Plugin-Class>${plugin.class}</Plugin-Class>
                        <Plugin-Dependencies>${plugin.dependencies}</Plugin-Dependencies>
                    </manifestEntries>
                </archive>
            </configuration>
            <executions>
                <execution>
                    <id>make-assembly</id>
                    <phase>package</phase>
                    <goals>
                        <goal>single</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

功能实现

```java
/**
 * 需要实现接口及增加Extension注解
 */
@Extension
public class EmailNotice implements Notice {

    @Override
    public boolean notice(List<Long> userIds) {
        // todo逻辑实现
        final ArrayList<Object> objects = Lists.newArrayList();
        System.out.println("email notice");
        return true;
    }
}
```

如果还有其他插件，也是类似的实现

### 3.插件使用

将之前打包的插件包放到一个路径包下，之后新建一个应用模版，就可以获取插件中的扩展点并使用

```java
public static void main(String[] args) {
    // 创建的时候，可以指定插件所在的包（其中可以有多个插件包）
    PluginManager pluginManager = new DefaultPluginManager(Paths.get("/u/plugins"));
    pluginManager.loadPlugins();
    pluginManager.startPlugins();

    List<Long> list = new ArrayList<>();
    final List<Notice> extensions = pluginManager.getExtensions(Notice.class);
    extensions.forEach(e -> e.notice(list));
    
    // 也可以获取制定插件中的实现
    extensions = pluginManager.getExtensions(Notice.class, "email-plugin");
    extensions.forEach(e -> e.notice(list));

    pluginManager.stopPlugins();
    pluginManager.unloadPlugins();
}
```

https://zhengw-tech.com/2022/01/02/pf4j-use/

