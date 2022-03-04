function loadEditionsForNewspaperOnDate(batchID, avisID, date, editionIndex, pageIndex) {
    var day = moment(date).format('YYYY-MM-DD');
    var url = `api/batch/${batchID}/${avisID}/${day}`
    $("#notice-div").empty();
    $("#state-div").empty();
    $("#batchOverview-table").empty();
    const $headline = $("#headline-div").empty();
    $("#primary-show").empty();

    $.getJSON(url)
        .done(function (newspaperDay) {
            $headline.append($("<a/>", {
                class: "btn btn-secondary",
                text: "Back to newspaper year",
                href: `#/newspaper/${avisID}/${day.split('-')[0]}/`
            }))
            $headline.append($("<a/>", {
                class: "btn btn-secondary",
                text: "Back to batch",
                href: `#/batch/${batchID}/`
            }))
            $headline.append($("<h1>").text(`Editions for ${avisID} on ${day}`));
            // console.log("Starting rendering of entites.");
            renderDayDisplay(newspaperDay, editionIndex, pageIndex);
        })
        .fail(function (jqxhr, textStatus, error) {
            $headline.append($("<h1/>").text(`${jqxhr.responseText}`));
        });
}


function noteSubmitHandler(event) {
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
    let url = parts.join("/") + "?" + query.toString();

    console.log(url)
    const notes = data.get('standardNote')+" "+ data.get('notes');

    $.ajax({
        type: "POST",
        url: url,
        data: notes,
        success: function () {
            alert("notes updated")
        },
        dataType: "json",
        contentType: "application/json"
    });
    location.reload();
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
        type: "DELETE",
        url: url,
        data: notes,
        success: function () {
            alert("note deleted")
        },
        dataType: "json",
        contentType: "application/json"
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
    if (editionIndex < 0 || editionIndex >= editions.length){
        $("#primary-show").text(`Edition ${editionIndex+1} not found. Day only has ${editions.length} editions`);
        return;
    }
    const edition = editions[editionIndex];

    const $dayCol = $("#dayCol");
    let $dayNotesTextArea = $("<textarea/>", {
        class: "userNotes",
        id: "dayNotes",
        type: "text",
        name: "notes"
    })


    let $dayNotesForm = $("<form>", {id: "dayNotesForm", action: "", method: "post"});

    let $dropDownDayNotes = $("<select/>",{class:"form-select",name:"standardNote"});

    $dropDownDayNotes.append($("<option>",{value:"",html:"",selected:"true"}));
    $dropDownDayNotes.append($("<option>",{value:"Udkom ikke pgr. strejke",html:"Udkom ikke pgr. strejke"}));
    $dayNotesForm.append($dropDownDayNotes)
    $dayNotesForm.append($("<label/>", {for: "dayNotes"}).text("Day notes"));
    $dayNotesForm.append($dayNotesTextArea);
    $dayNotesForm.append($("<input/>", {
        id: "dayNotesFormSubmit",
        type: "submit",
        name: "submit",
        form: "dayNotesForm"
    }));
    $dayNotesForm.append($("<input/>", {type: "hidden", name: "batch", value: newspaperDay.batchid}));
    $dayNotesForm.append($("<input/>", {type: "hidden", name: "avis", value: newspaperDay.avisid}));
    $dayNotesForm.append($("<input/>", {type: "hidden", name: "date", value: newspaperDay.date}));
    $dayNotesForm.submit(noteSubmitHandler);
    $dayCol.append($dayNotesForm);


    for(let i = 0; i < newspaperDay.notes.length; i++){
        let $dayFormDiv = $("<div/>", {class:"dayFormDiv"});
        let $dayForm = $("<form>", {action: "", method: "delete"});
        $dayForm.append($("<input/>", {type: "hidden", name: "batch", value: newspaperDay.batchid}));
        $dayForm.append($("<input/>", {type: "hidden", name: "id", value: newspaperDay.notes[i].id}));
        let $dayNote = $("<textarea/>",{class:"userNotes",type:"text",name:"notes",text:newspaperDay.notes[i].note, readOnly:"true"});
        $dayForm.append($dayNote);
        $dayForm.append($("<input/>", {
            type: "submit",
            name: "submit",
            value:"Delete"
        }));
        $dayForm.append($("<label/>",{text:"-"+newspaperDay.notes[i].username + " " + newspaperDay.notes[i].created}))
        $dayForm.submit(noteDeleteHandler);
        $dayFormDiv.append($dayForm);
        $dayCol.append($dayFormDiv);
    }

    const $editionCol = $("#editionCol");

    const $editionNav = $("<div/>", {class: 'btn-toolbar mb-2 mb-md-0'})
        .append($("<div/>", {
            class: 'btn-group mr-2 d-flex justify-content-evenly flex-wrap',
            id: 'edition-nav'
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
            class: "userNotes",
            id: "editionNotes",
            type: "text",
            name: "notes"
        })


        let $editionNotesForm = $("<form>", {id: "editionNotesForm",action: "api/editionNotes", method: "post"});

        let $dropDownEditionNotes = $("<select/>",{class:"form-select",name:"standardNote"});

        $dropDownEditionNotes.append($("<option>",{value:"",html:"",selected:"true"}));
        $dropDownEditionNotes.append($("<option>",{value:"Ugyldig edition",html:"Ugyldig edition"}));
        $editionNotesForm.append($dropDownEditionNotes)
        $editionNotesForm.append($("<label/>", {for: "editionNotes"}).text("Edition notes"));
        $editionNotesForm.append($editionNotesTextArea);
        $editionNotesForm.append($("<input/>", {
            id: "editionNotesFormSubmit",
            type: "submit",
            name: "submit",
            form: "editionNotesForm"
        }));
        $editionNotesForm.append($("<input/>", {type: "hidden", name: "batch", value: edition.batchid}));
        $editionNotesForm.append($("<input/>", {type: "hidden", name: "avis", value: edition.avisid}));
        $editionNotesForm.append($("<input/>", {type: "hidden", name: "date", value: edition.date}));
        $editionNotesForm.append($("<input/>", {type: "hidden", name: "edition", value: edition.edition}));
        $editionNotesForm.submit(noteSubmitHandler);
        $editionCol.append($editionNotesForm);

        if(edition.notes) {
            for (let i = 0; i < edition.notes.length; i++) {
                let $editionFormDiv = $("<div/>", {class: "dayFormDiv"});
                let $editionForm = $("<form>", {action: "", method: "delete"});
                $editionForm.append($("<input/>", {type: "hidden", name: "batch", value: edition.batchid}));
                $editionForm.append($("<input/>", {type: "hidden", name: "id", value: edition.notes[i].id}));
                let $editionNote = $("<textarea/>", {
                    class: "userNotes",
                    type: "text",
                    name: "notes",
                    text: edition.notes[i].note,
                    readOnly: "true"
                });
                $editionForm.append($editionNote);
                $editionForm.append($("<input/>", {
                    type: "submit",
                    name: "submit",
                    value: "Delete"
                }));
                $editionForm.append($("<label/>", {text: "-" + edition.notes[i].username + " " + edition.notes[i].created}))
                $editionForm.submit(noteDeleteHandler);
                $editionFormDiv.append($editionForm);
                $editionCol.append($editionFormDiv);
            }
        }
        if (edition.pages.length === 1) {
            renderSinglePage(edition.pages[0]);
        } else {
            renderEdition(edition, pageIndex);
        }

        // alert($editionNotesForm.length) // if is == 0, not found form

    });
}

function renderSinglePage(page) {
    let $pageDisplay = $("#primary-show");

    const date = moment(page.editionDate).format("YYYY-MM-DD");
    const $pageCol = $("#pageCol");


    let $pageNotesTextArea = $("<textarea/>", {
        class: "userNotes",
        id: "pageNotes",
        type: "text",
        name: "notes"
    })


    let $pageNotesForm = $("<form>", {id: "pageNotesForm",action: "", method: "post"});

    let $dropDownPageNotes = $("<select/>",{class:"form-select",name:"standardNote"});

    $dropDownPageNotes.append($("<option>",{value:"",html:"",selected:"true"}));
    $dropDownPageNotes.append($("<option>",{value:"Utydelig side",html:"Utydelig side"}));
    $pageNotesForm.append($dropDownPageNotes)
    $pageNotesForm.append($("<label/>", {for: "pageNotes"}).text("Page notes"));
    $pageNotesForm.append($pageNotesTextArea);
    $pageNotesForm.append($("<input/>", {
        id: "pageNotesFormSubmit",
        type: "submit",
        name: "submit",
        form: "pageNotesForm"
    }));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "batch", value: page.batchid}));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "avis", value: page.avisid}));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "date", value: page.editionDate}));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "edition", value: page.editionTitle}));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "section", value: page.sectionTitle}));
    $pageNotesForm.append($("<input/>", {type: "hidden", name: "page", value: page.pageNumber}));
    $pageNotesForm.submit(noteSubmitHandler);
    $pageCol.append($pageNotesForm);

    if(page.notes) {
        for (let i = 0; i < page.notes.length; i++) {
            let $pageFormDiv = $("<div/>", {class: "pageFormDiv"});
            let $pageForm = $("<form>", {action: "", method: "delete"});
            $pageForm.append($("<input/>", {type: "hidden", name: "batch", value: page.batchid}));
            $pageForm.append($("<input/>", {type: "hidden", name: "id", value: page.notes[i].id}));
            let $pageNote = $("<textarea/>", {
                class: "userNotes",
                type: "text",
                name: "notes",
                text: page.notes[i].note,
                readOnly: "true"
            });
            $pageForm.append($pageNote);
            $pageForm.append($("<input/>", {
                type: "submit",
                name: "submit",
                value: "Delete"
            }));
            $pageForm.append($("<label/>", {text: "-" + page.notes[i].username + " " + page.notes[i].created}))
            $pageForm.submit(noteDeleteHandler);
            $pageFormDiv.append($pageForm);
            $pageCol.append($pageFormDiv);
        }
    }
    let $contentRow = $("#contentRow");
    $contentRow.append($pageCol);

    let $fileAndProblemsCol = $("<div/>", {class: "col-8"})

    if (page.problems) {
        $fileAndProblemsCol.append($("<p>").text("Problems: ").append($("<pre>").text(JSON.stringify(
            JSON.parse(page.problems),
            ['type', 'filereference', 'description'],
            2))));
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
        type: "GET",
        url: url,
        xhrFields: {responseType: 'arraybuffer'},
        beforeSend: function () {
            result.text(`Loading Page`);
        },
        success: function (data) {
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
        },
        error: function (jqXHR, errorType, exception) {
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
        $pageDisplay.text(`Page ${pageIndex+1} not found. Edition only has ${pages.length} pages`);
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



