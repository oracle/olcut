#!/usr/bin/bash

_java_completions()
{
    # set -x
    local completion_jar=${OLCUT_HOME:-$HOME/.olcut}/completions/completion.jar
    local IFS=$'\n'
    local cur="${COMP_WORDS[$COMP_CWORD]}"
    for i in "${!COMP_WORDS[@]}"; do
        if [[ "${COMP_WORDS[$i]}" == -cp ]] || [[ "${COMP_WORDS[$i]}" == --class-path ]]; then
            local j=i+1
            local jar_file="${COMP_WORDS[$j]}"
        fi
    done

    if [ -f $jar_file ] && [[ ${jar_file: -4} == ".jar" ]]; then # We have a jar file that exists
        local completion_file=$($JAVA_HOME/bin/java -jar $completion_jar $jar_file)
        local classes=$(awk '$0 !~ sprintf("%c", 30)' $completion_file)
        local class=$(comm -12 <(echo $classes | tr ' ' '\n' | sort) <(echo ${COMP_WORDS[@]} | tr ' ' '\n' | sort))

        if [ -z $class ]; then # no main-method filled
            COMPREPLY=($(compgen -W "$classes" -- "$cur"))
        elif [ -n $class ]; then
            local suggestions=($(awk -v cur=$cur -v class=$class 'BEGIN {FS="\x1F"; class_reg="\x1E" class; cur_reg="^" cur} $0 ~ class_reg {if($2 != "\x00" && $2 ~ cur_reg) {printf("%s  %s\x1F%s\n", $2, $3, $4)} else if($3 ~ cur_reg) {printf("%s\x1F%s\n", $3, $4)}}' $completion_file | column -t -s $'\x1F'))

            if [ "${#suggestions[@]}" == "1" ]; then
                local command="${suggestions[0]/%\ */}"
                COMPREPLY=("$command")
            else
                COMPREPLY=("${suggestions[@]}")
            fi
        fi
    elif [[ "${COMP_WORDS[-2]}" == -cp ]] || [[ "${COMP_WORDS[-2]}" == --class-path ]]; then # if we're trying to complete after classpath
        COMPREPLY=()
    else
        COMPREPLY=()
    fi


}

complete -o bashdefault -o default -F _java_completions java
