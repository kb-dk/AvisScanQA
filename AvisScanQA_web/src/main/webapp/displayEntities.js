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
    const notes = data.get('notes');

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
    $("#contentRow");


    const $dayCol = $("#dayCol");

    let $dayNotesForm = $("<form>", {id: "dayNotesForm", action: "", method: "post"});
    $dayNotesForm.append($("<label/>", {for: "dayNotes"}).text("Day notes"));
    $dayNotesForm.append($("<textarea/>", {
        class: "userNotes",
        id: "dayNotes",
        type: "text",
        name: "notes"
    }).text(newspaperDay.notes))
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

    const $editionCol = $("#editionCol");

    const $editionNav = $("<div/>", {class: 'btn-toolbar mb-2 mb-md-0'})
        .append($("<div/>", {
            class: 'btn-group mr-2 d-flex justify-content-evenly flex-wrap',
            id: 'edition-nav'
        }));
    $editionCol.append($editionNav);

    const editionShow = $("<div/>", {id: 'edition-show'}).append($("<h1>", {text: "show me a newspaper"}));
    $editionCol.append(editionShow);

    let editions = newspaperDay.editions;
    for (let i = 0; i < editions.length; i++) {
        const edition = editions[i];
        if (i === editionIndex) {
            let editionButton = $("<button/>", {
                class: "btn btn-sm btn-outline-secondary active",
                text: edition.edition
            });
            $editionNav.append(editionButton);
        } else {
            const otherEditionLink = $("<a/>").attr({
                href: editEntityIndexInHash(location.hash, i),
                class: 'btn btn-sm btn-outline-secondary',
                text: edition
            });
            $editionNav.append(otherEditionLink);
        }
    }
    $("#edition-show").load("entityDisplay.html", function () {
        var edition = editions[editionIndex];
        if (edition.pages.length === 1) {
            renderSinglePage(edition.pages[0]);
        } else {
            renderEdition(edition, pageIndex);
        }

        const $editionForm = $("<form>", {id: "editionNotesForm", action: "api/editionNotes", method: "post"});
        $editionForm.append($("<label/>").text("Edition notes").attr("for", "editionNotes"));
        $editionForm.append($("<textarea/>", {
            class: "userNotes",
            id: "editionNotes",
            type: "text",
            name: "notes"
        }).text(edition.notes))
        $editionForm.append($("<input/>", {id: "submit", type: "submit", name: "submit", form: "editionNotesForm"}));
        $editionForm.append($("<input/>", {type: "hidden", name: "batch", value: edition.batchid}));
        $editionForm.append($("<input/>", {type: "hidden", name: "avis", value: edition.avisid}));
        $editionForm.append($("<input/>", {type: "hidden", name: "date", value: edition.date}));
        $editionForm.append($("<input/>", {type: "hidden", name: "edition", value: edition.edition}));

        $editionCol.append($editionForm);
        // alert($editionNotesForm.length) // if is == 0, not found form

        $editionForm.submit(noteSubmitHandler);

    });
}

function renderSinglePage(page) {
    let $pageDisplay = $("#primary-show");

    const date = moment(page.editionDate).format("YYYY-MM-DD");
    const $pageCol = $("#pageCol");

    let $pageForm = $("<form/>", {id: "pageNotesForm", action: "", method: "post"});
    $pageForm.append($("<label/>", {for: "pageNotes"}).text("Page notes"));
    $pageForm.append($("<textarea/>", {
        class: "userNotes",
        id: "pageNotes",
        type: "text",
        name: "notes"
    }).text(page.notes));
    //TODO proper values for all fields
    $pageForm.append($("<input/>", {type: "hidden", name: "batch", value: page.batchid}));
    $pageForm.append($("<input/>", {type: "hidden", name: "avis", value: page.avisid}));
    $pageForm.append($("<input/>", {type: "hidden", name: "date", value: date}));
    $pageForm.append($("<input/>", {type: "hidden", name: "edition", value: page.editionTitle}));
    $pageForm.append($("<input/>", {type: "hidden", name: "section", value: page.sectionTitle}));
    $pageForm.append($("<input/>", {type: "hidden", name: "page", value: page.pageNumber}));
    $pageForm.append($("<input/>", {type: "submit", name: "submit", id: "pageNotesFormSubmit", form: "pageNotesForm"}));
    $pageForm.submit(noteSubmitHandler);
    $pageCol.append($pageForm);

    let $contentRow = $("#contentRow");
    $contentRow.append($pageCol);

    let $fileAndProblemsCol = $("<div/>", {class: "col-8"})
        .append($("<a>").text("Vis fil: " + page.origRelpath));
    if (page.problems) {
        $fileAndProblemsCol.append($("<p>").text("Problems: ").append($("<pre>").text(JSON.stringify(
            JSON.parse(page.problems),
            ['type', 'filereference', 'description'],
            2))));
    }
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

function renderEdition(entity, pageIndex) {
    let $pageNav = $("#page-nav");
    let pages = entity.pages;
    for (var i = 0; i < pages.length; i++) {
        if (i === pageIndex) {
            const button = $("<button/>").attr({class: 'btn btn-sm btn-outline-secondary active'}).text(i + 1);
            $pageNav.append(button);
        } else {
            const link = $("<a/>").attr({
                href: editPageIndexInHash(location.hash, i),
                class: 'btn btn-sm btn-outline-secondary'
            }).text(i + 1);
            $pageNav.append(link);
        }
    }

    renderSinglePage(pages[pageIndex]);
}


function editEntityIndexInHash(origHash, newEntityIndex) {
    var hashParts = origHash.split("/");
    hashParts[hashParts.length - 3] = newEntityIndex;
    var newHash = hashParts.join("/");
    return newHash;
}

function editPageIndexInHash(origHash, newPageIndex) {
    var hashParts = origHash.split("/");
    hashParts[hashParts.length - 2] = newPageIndex;
    var newHash = hashParts.join("/");
    return newHash;
}

function renderBatchTable(filter =""){
    let $tableDiv = $("#batchOverview-div");

    $tableDiv.load("batchTable.html",function (){

    })
    let $table = $("#batchTable")
    console.log($table.data());
    $table.bootstrapTable('refreshOptions', {
        filterOptions: {
            filterAlgorithm: 'or'
        }
    })
    $table.bootstrapTable('filterBy', {
        state:filter
    })
    console.log($table.data());
}