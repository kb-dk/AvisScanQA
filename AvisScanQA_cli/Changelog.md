Version 1.0: Newspaper QA with CLI and web application
1.1:
1.2: Ignores JPEG folder in batch
1.3: No longer creates an acknowledge file. Fixed issue with Firefox not showing some input boxes.
1.4: Fixed incorrect SQL file.
1.5: Fixed storage of tif file paths. Now stores the whole path in the database, instead of up to batchid.
1.6: Logs error if network is down. Can't check if xml schema is correct, if it can't connect to loc.gov website.
1.7: Ignore jpg/jpeg files while checking for checksums. Now collects error, if alto file doesn't contain expected data.
1.8: Fixed web error in http get with foldername containing special character. Fixed issue if batch only contains 1 page, previously unable to read it.
1.9: During matching of event and reference in DatabaseRegister. Reference is null, if the error is an checksumerror. And thereby fails.
1.10: Now supports batch filenames only containing a year. Statistics of completed batches per month for a year can be downloaded as a csv file in webQA. WebQA user experience updates, sorted tables, unneeded alert removed, deletion of notes alert. WebQA can now display alto text contents. WebQA now displays jpeg rather than tiff.
1.11: Updated SQL schema. Removed unused library of Tiff integration. 
1.12: Statistics updated to show correct date. Added completed pages to statistics. Color changes. Changed default note dropdowns content. Fixed bug with changing years in calendar.
1.13: Updated error reporting for autoQA, added dropdown option for day notes. Bug fix for calendar year changes