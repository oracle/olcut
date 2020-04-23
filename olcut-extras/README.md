# olcut-extras

## Java autocompletion

Olcut provides basic java completion in bash for main methods and configuration arguments through a script in `olcut-extras/scripts/java-completion.bash`.

It currently supports a single fat jar. It will complete all main methods, and will complete `Options`-based command-line arguments showing usage strings for any class that uses `Options` to process arguments. It uses java to generate the completions and caches completions for individual jars in `$OLCUT_HOME/completions`, defaulting to `$HOME/.olcut/completions` if `$OLCUT_HOME` is not set. It also looks in `$OLCUT_HOME` for the jar file it needs to scan and generate completions. Thus installation would look something like:

```
# from the root of the olcut project
> mvn assembly:assembly -pl olcut-extras
> mkdir -p $OLCUT_HOME/completions # or mkdir -p ~/.olcut/completions
> cp olcut-extras/target/olcut-extras*-jar-with-dependencies.jar $OLCUT_HOME/completions/completion.jar
> source olcut-extras/scripts/java-completion.bash # also add this line to your .bashrc
```
