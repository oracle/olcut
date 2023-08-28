Test resources are ported from JSON using this jshell snippet.

```java
import com.oracle.labs.mlrg.olcut.config.*;
import com.oracle.labs.mlrg.olcut.config.json.*;
import com.oracle.labs.mlrg.olcut.config.protobuf.*;
ConfigurationManager.addFileFormatFactory(new JsonConfigFactory())
ConfigurationManager.addFileFormatFactory(new ProtoConfigFactory())
var p = Paths.get("olcut-config-json","src","test","resources","com","oracle","labs","mlrg","olcut","config","json","test")
var l = Files.list(p).filter(i -> i.getFileName().toString().contains("json")).toList()
for (var f : l) {
    var newName = f.getFileName().toString().split(".json")[0] + ".pb";
    try {
        var cm = new ConfigurationManager(f.toString());
        cm.save(new File(newName), true);
    } catch (Exception e) {
        System.out.println("File " + f + " was unhappy.");
    }
}
```