The tool requires a tags.txt file on the root.

The information can be retrieved from the database using the next SQL:

SELECT m.name as "tag", ea.perm_id as "experiment" 
FROM metaproject_assignments_all ma 
LEFT JOIN metaprojects m ON (m.id = ma.mepr_id) 
LEFT JOIN experiments_all ea ON (ea.id = ma.expe_id);

It follows the next format, including quotes, one tag per line:
"TagName"\t"permId"