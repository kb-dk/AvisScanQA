let editionJsonData;

function loadEditionsForNewspaperOnDate(batchID, avisID, date, editionIndex, pageIndex) {
    let day = moment(date).format('YYYY-MM-DD');
    let nextDay = moment(date).add(1,'d').format("YYYY-MM-DD")
    let pastDay = moment(date).subtract(1,'d').format("YYYY-MM-DD")
    var url = `api/batch/${batchID}/${avisID}/${day}`
    $("#notice-div").empty();
    $("#state-div").empty();
    $("#batchOverview-table").empty();
    const $headline = $("#headline-div").empty();
    $("#primary-show").empty();
    $.getJSON("api/config").done(function (data){editionJsonData = data.edition})

    $.getJSON(url)
        .done(function (newspaperDay) {
            console.log(day === newspaperDay.batch.startDate);
            console.log(day === newspaperDay.batch.endDate);
            $headline.append($("<a/>", {
                class: "btn btn-secondary",
                text: "Back to newspaper year",
                href: `#/newspaper/${avisID}/${day.split('-')[0]}/`
            }))
            $headline.append($("<a/>", {
                class: "btn btn-secondary", text: "Back to batch", href: `#/batch/${batchID}/`
            }))
            let $buttonForward = $("<a/>", {
                class: "btn btn-secondary bi bi-caret-right",href: `#/newspapers/${batchID}/${avisID}/${nextDay}/0/0/`
            }).css({"float":"right"})
            if(day === newspaperDay.batch.endDate){

                $buttonForward.css({
                    "background-color" : "#6c757d9e",
                    "border-color":"#6c757d9e",
                    "pointer-events":"none"
                })
            }
            $headline.append($buttonForward)
            let $buttonBack = $("<a/>", {
                class: "btn btn-secondary bi bi-caret-left", href: `#/newspapers/${batchID}/${avisID}/${pastDay}/0/0/`
            }).css({"float":"right"})
            if(day === newspaperDay.batch.startDate){
                $buttonBack.css({
                    "background-color" : "#6c757d9e",
                    "border-color":"#6c757d9e",
                    "pointer-events":"none"
                })
            }
            $headline.append($buttonBack)
            $headline.append($("<h1>").text(`Editions for ${avisID} on ${day}`));
            // console.log("Starting rendering of entites.");
            renderDayDisplay(newspaperDay, editionIndex, pageIndex);
        })
        .fail(function (jqxhr, textStatus, error) {
            $headline.append($("<h1/>").text(`${jqxhr.responseText}`));
        });
}


function noteSubmitHandler(event, url) {
    console.log(event)
    event.preventDefault(); // <- cancel event

    const data = new FormData(event.target);

    let parts = ["api", "notes", data.get('batch')]
    var query = new URLSearchParams();

    query.append("avis", data.get('avis'));
    query.append("date", data.get('date'));
    query.append("edition", data.get('edition'));
    query.append("section", data.get('section'));
    query.append("page", data.get('page'));

    // let url = parts.filter(x => x).join("/")

        url = parts.join("/") + "?" + query.toString();

    console.log(url)
    const notes = data.get('standardNote') + " " + data.get('notes');

    $.ajax({
        type: "POST", url: url, data: notes, success: function () {
            alert("notes updated")
        }, dataType: "json", contentType: "application/json"
    });
    //location.reload();
    // alert('Handler for .submit() called.');
    return false;  // <- cancel event
}

function noteDeleteHandler(event) {
    event.preventDefault(); // <- cancel event

    const data = new FormData(event.target);

    let parts = ["api", "notes", data.get('batch')]
    var query = new URLSearchParams();

    query.append("id", data.get('id'));

    // let url = parts.filter(x => x).join("/")
    let url = parts.join("/") + "?" + query.toString();

    const notes = data.get('notes');

    $.ajax({
        type: "DELETE", url: url, data: notes, success: function () {
            alert("note deleted")
        }, dataType: "json", contentType: "application/json"
    });
    location.reload();
    // alert('Handler for .submit() called.');
    return false;  // <- cancel event
}


function initComponents() {
    let $primary = $("#primary-show");
    const $contentRow = $("<div/>", {class: "row", id: "contentRow"});
    const $dayCol = $("<div/>", {id: "dayCol", class: "col"});
    const $editionCol = $("<div/>", {id: "editionCol", class: "col"});
    const $pageCol = $("<div/>", {id: "pageCol", class: "col"});
    let $pageNavBtnToolbar = $("<div/>", {class: "btn-toolbar mb-2 mb-md-0"});
    let $pageNav = $("<div/>", {class: "btn-group mr-2 d-flex justify-content-evenly flex-wrap", id: "page-nav"});
    $contentRow.append($dayCol);
    $contentRow.append($editionCol);
    $pageNavBtnToolbar.append($pageNav);
    $pageCol.append($pageNavBtnToolbar);
    $contentRow.append($pageCol);
    $primary.append($contentRow);
}

function renderDayDisplay(newspaperDay, editionIndex, pageIndex) {
    $("#primary-show").empty();
    initComponents();

    let editions = newspaperDay.editions;
    if (editionIndex < 0 || editionIndex >= editions.length) {
        $("#primary-show").text(`Edition ${editionIndex + 1} not found. Day only has ${editions.length} editions`);
        return;
    }
    const edition = editions[editionIndex];

    const $dayCol = $("#dayCol");
    let $dayNotesTextArea = $("<textarea/>", {
        class: "userNotes", id: "dayNotes", type: "text", name: "notes"
    })


    let $dayNotesForm = $("<form>", {id: "dayNotesForm", action: "", method: "post"});

    const formRow1 = $("<div>", {class: "form-row"})
    const formRow2 = $("<div>", {class: "form-row"})
    $dayNotesForm.append(formRow1);
    $dayNotesForm.append(formRow2);


    let $dropDownDayNotes = $("<select/>", {class: "form-select", name: "standardNote"});

    $dropDownDayNotes.append($("<option>", {value: "", html: "", selected: "true"}));
    for(let option of editionJsonData.dropDownStandardMessage.dayDropDown.options){
        $dropDownDayNotes.append($("<option>", {value: option, html: option}));
    }

    formRow1.append($dropDownDayNotes)
    formRow1.append($("<label/>", {for: "dayNotes"}).text("Day notes"));
    formRow2.append($dayNotesTextArea);
    formRow2.append($("<input/>", {
        id: "dayNotesFormSubmit", type: "submit", name: "submit", form: "dayNotesForm"
    }));
    $dayNotesForm.append($("<input/>", {type: "hidden", name: "batch", value: newspaperDay.batchid}));
    $dayNotesForm.append($("<input/>", {type: "hidden", name: "avis", value: newspaperDay.avisid}));
    $dayNotesForm.append($("<input/>", {type: "hidden", name: "date", value: newspaperDay.date}));
    $dayNotesForm.submit(noteSubmitHandler);
    $dayCol.append($dayNotesForm);


    for (let i = 0; i < newspaperDay.notes.length; i++) {

        let $dayForm = $("<form>", {action: "", method: "delete"});

        $dayForm.append($("<input/>", {type: "hidden", name: "batch", value: newspaperDay.batchid}));
        $dayForm.append($("<input/>", {type: "hidden", name: "id", value: newspaperDay.notes[i].id}));

        const formRow = $("<div>", {class: "form-row"})
        $dayForm.append(formRow);

        formRow.append($("<label/>", {text: "-" + newspaperDay.notes[i].username + " " + newspaperDay.notes[i].created}))
        let $dayNote = $("<textarea/>", {
            class: "userNotes", type: "text", name: "notes", text: newspaperDay.notes[i].note, readOnly: "true", disabled: true
        });
        formRow.append($dayNote);
        formRow.append($("<button/>",{class:"bi bi-x-circle-fill",type:"submit"}).css({"border":"none","background-color":"#fff"}));

        $dayForm.submit(noteDeleteHandler);

        $dayCol.append($dayForm);
    }

    const $editionCol = $("#editionCol");

    const $editionNav = $("<div/>", {class: 'btn-toolbar mb-2 mb-md-0'})
        .append($("<div/>", {
            class: 'btn-group mr-2 d-flex justify-content-evenly flex-wrap', id: 'edition-nav'
        }));
    $editionCol.append($editionNav);

    const editionShow = $("<div/>", {id: 'edition-show'}).append($("<h1>", {text: "show me a newspaper"}));
    $editionCol.append(editionShow);

    for (let i = 0; i < editions.length; i++) {
        const edition = editions[i];
        const link = $("<a/>").attr({
            href: editEntityIndexInHash(location.hash, i),
            class: `btn btn-sm btn-outline-secondary ${(i === editionIndex ? "active" : "")}`,
        }).text(edition.edition);
        $editionNav.append(link);
    }
    $("#edition-show").load("editionDisplay.html", function () {

        let $editionNotesTextArea = $("<textarea/>", {
            class: "userNotes", id: "editionNotes", type: "text", name: "notes"
        })


        let $editionNotesForm = $("<form>", {id: "editionNotesForm", action: "api/editionNotes", method: "post"});

        const formRow1 = $("<div>", {class: "form-row"})
        const formRow2 = $("<div>", {class: "form-row"})
        $editionNotesForm.append(formRow1);
        $editionNotesForm.append(formRow2);

        let $dropDownEditionNotes = $("<select/>", {class: "form-select", name: "standardNote"});

        $dropDownEditionNotes.append($("<option>", {value: "", html: "", selected: "true"}));
        for(let option of editionJsonData.dropDownStandardMessage.udgDropDown.options){
            $dropDownEditionNotes.append($("<option>", {value:option, html: option}));
        }

        formRow1.append($dropDownEditionNotes)
        formRow1.append($("<label/>", {for: "editionNotes"}).text("Edition notes"));
        formRow2.append($editionNotesTextArea);
        formRow2.append($("<input/>", {
            id: "editionNotesFormSubmit", type: "submit", name: "submit", form: "editionNotesForm"
        }));
        $editionNotesForm.append($("<input/>", {type: "hidden", name: "batch", value: edition.batchid}));
        $editionNotesForm.append($("<input/>", {type: "hidden", name: "avis", value: edition.avisid}));
        $editionNotesForm.append($("<input/>", {type: "hidden", name: "date", value: edition.date}));
        $editionNotesForm.append($("<input/>", {type: "hidden", name: "edition", value: edition.edition}));
        $editionNotesForm.submit(noteSubmitHandler);
        $editionCol.append($editionNotesForm);

        for (let i = 0; i < edition.notes.length; i++) {
            const note = edition.notes[i];

            let $editionForm = $("<form>", {id:"editionForm", action: "", method: "delete"});
            $editionForm.append($("<input/>", {type: "hidden", name: "batch", value: edition.batchid}));
            $editionForm.append($("<input/>", {type: "hidden", name: "id", value: note.id}));

            const formRow = $("<div>", {class: "form-row"})
            $editionForm.append(formRow);

            formRow.append($("<label/>", {text: "-" + note.username + " " + note.created}))
            let $editionNote = $("<textarea/>", {
                class: "userNotes", type: "text", name: "notes", text: note.note, readOnly: "true", disabled: true
            });
            formRow.append($editionNote);
            formRow.append($("<button/>",{class:"bi bi-x-circle-fill",type:"submit"}).css({"border":"none","background-color":"#fff"}));


            $editionForm.submit(noteDeleteHandler);
            $editionCol.append($editionForm);
        }

        if (edition.pages.length === 1) {
            renderSinglePage(edition.pages[0]);
        } else {
            renderEdition(edition, pageIndex);
        }

    });
}

function renderSinglePage(page) {
    let $pageDisplay = $("#primary-show");

    const date = moment(page.editionDate).format("YYYY-MM-DD");
    const $pageCol = $("#pageCol");


    let $pageNotesForm = $("<form>", {id: "pageNotesForm", action: "", method: "post"});

    const formRow1 = $("<div>", {class: "form-row"})
    const formRow2 = $("<div>", {class: "form-row"})
    $pageNotesForm.append(formRow1);
    $pageNotesForm.append(formRow2);
    let $standardMessageSelect = $("<select/>", {
        class: "form-select", name: "standardNote"
    }).append($("<option>", {value: "", html: "", selected: "true"}));
    for (let option of editionJsonData.dropDownStandardMessage.pageDropDown.options){
        $standardMessageSelect.append($("<option>", {
            value: option, html: option
        }))
    }
    formRow1.append($standardMessageSelect)


    formRow1.append($("<label/>", {for: "pageNotes"}).text("Page notes"));


    formRow2.append($("<textarea/>", {
        class: "userNotes", id: "pageNotes", type: "text", name: "notes"
    }));
    formRow2.append($("<input/>", {
        id: "pageNotesFormSubmit", type: "submit", name: "submit", form: "pageNotesForm"
    }));

    $pageNotesForm.append($("<input/>", {type: "hidden", name: "batch", value: page.batchid}));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "avis", value: page.avisid}));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "date", value: page.editionDate}));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "edition", value: page.editionTitle}));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "section", value: page.sectionTitle}));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "page", value: page.pageNumber}));

    $pageNotesForm.submit(noteSubmitHandler);
    $pageCol.append($pageNotesForm);

    for (let i = 0; i < page.notes.length; i++) {
        // let $pageFormDiv = $("<div/>", {class: "pageFormDiv"});
        let $pageForm = $("<form>", {action: "", method: "delete"});
        $pageForm.append($("<input/>", {type: "hidden", name: "batch", value: page.batchid}));

        const note = page.notes[i];
        $pageForm.append($("<input/>", {type: "hidden", name: "id", value: note.id}));

        const formRow = $("<div>", {class: "form-row"})
        $pageForm.append(formRow);

        let $pageNote = $("<textarea/>", {
            class: "userNotes",
            type: "text",
            name: "notes",
            text: note.note,
            readOnly: "true",
            disabled: true
        });
        formRow.append($("<label/>", {for: $pageNote.uniqueId().attr("id"), text: `-${note.username} ${note.created}`}))
        formRow.append($pageNote);
        formRow.append($("<button/>",{class:"bi bi-x-circle-fill",type:"submit"}).css({"border":"none","background-color":"#fff"}));
        $pageForm.submit(noteDeleteHandler);
        $pageCol.append($pageForm);
    }

    let $contentRow = $("#contentRow");
    $contentRow.append($pageCol);

    let $fileAndProblemsCol = $("<div/>", {class: "col-8"})

    if (page.problems) {
        $fileAndProblemsCol.append($("<p>").text("Problems: ").append($("<pre>").text(JSON.stringify(JSON.parse(page.problems), ['type', 'filereference', 'description'], 2))));
    }

    loadImage(page.origRelpath, $fileAndProblemsCol);

    let $infoDumpRow = $("<div/>", {class: "row"});
    $infoDumpRow.append($fileAndProblemsCol);


    let $entityInfoCol = $("<div/>", {class: "col-4"});
    let infoHtml = `Edition titel: ${page.editionTitle}<br>`;
    infoHtml += `Section titel: ${page.sectionTitle}<br>`;
    infoHtml += `Side nummer: ${page.pageNumber}<br>`;
    infoHtml += `Enkelt side: ${page.singlePage}<br>`;
    infoHtml += `Afleverings dato: ${moment(page.deliveryDate).format("YYYY-MM-DD")}<br>`;
    infoHtml += `Udgivelses dato: ${date}<br>`;
    infoHtml += `Format type: ${page.formatType}<br>`;

    $entityInfoCol.html(infoHtml);
    $infoDumpRow.append($entityInfoCol);
    $pageDisplay.append($infoDumpRow);


}


function loadImage(filename, element) {
    let result = $("<div>");
    element.append(result);
    const url = "api/file/?file=" + filename;
    return $.ajax({
        type: "GET", url: url, xhrFields: {responseType: 'arraybuffer'}, beforeSend: function () {
            result.text(`Loading Page`);
        }, success: function (data) {
            var tiff = new Tiff({buffer: data});
            var width = tiff.width();
            var height = tiff.height();
            var canvas = tiff.toCanvas();
            if (canvas) {
                canvas.setAttribute('style', `width:100%;`);
                canvas.download = filename;
                canvas.title = filename;
                canvas.filename = filename;
                result.empty().append($("<a>", {href: url}).text(filename)).append(canvas);
            }
        }, error: function (jqXHR, errorType, exception) {
            result.empty().text(`Failed to read file ${filename}`);
        }

    });

}

function renderEdition(entity, pageIndex) {
    let $pageNav = $("#page-nav");
    let pages = entity.pages;

    for (let i = 0; i < pages.length; i++) {
        let nrOfProblems = pages[i].problems.length;
        const link = $("<a/>").attr({
            href: editPageIndexInHash(location.hash, i),

            class: `btn btn-sm btn-outline-secondary ${(i === pageIndex ? "active" : "")} ${(nrOfProblems > 0 ? "btn-warning" : "")}`,
        }).text(i + 1);
        $pageNav.append(link);
    }
    if (pageIndex >= 0 && pageIndex < pages.length) {
        renderSinglePage(pages[pageIndex]);
    } else {
        let $pageDisplay = $("#primary-show");
        $pageDisplay.text(`Page ${pageIndex + 1} not found. Edition only has ${pages.length} pages`);
    }
}


function editEntityIndexInHash(origHash, newEntityIndex) {
    var hashParts = origHash.split("/");
    hashParts[hashParts.length - 3] = newEntityIndex;
    return hashParts.join("/");
}

function editPageIndexInHash(origHash, newPageIndex) {
    var hashParts = origHash.split("/");
    hashParts[hashParts.length - 2] = newPageIndex;
    return hashParts.join("/");
}



