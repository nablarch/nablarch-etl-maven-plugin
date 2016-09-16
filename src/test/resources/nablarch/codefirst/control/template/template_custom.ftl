OPTIONS (
<#if control.requiredHeader>
SKIP = 1,
</#if>
)
LOAD DATA
CHARACTERSET ${control.charset}
REPLACE
PRESERVE BLANKS
INTO TABLE ${control.tableName}
FIELDS
TERMINATED BY '${control.fieldSeparator}'
OPTIONALLY ENCLOSED BY '${control.quote}'
TRAILING NULLCOLS
(
<#list control.columnNames as columnName>
${columnName}<#if columnName_has_next>,</#if>
</#list>
)