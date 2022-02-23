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


function renderEntityDisplay(currentEntities, currentEntityIndex, pageIndex) {
    let $primary = $("#primary-show");
    $primary.empty();
    let $dayNotesForm = $("<form>", {id: "dayNotesForm", action: "", method: "post"});
    $dayNotesForm.append($("<label/>", {for: "dayNotes"}).text("Day notes"));
    $dayNotesForm.append($("<textarea/>", {class: "userNotes", id: "dayNotes", type: "text", name: "notes"}))
    $dayNotesForm.append($("<input/>", {
        id: "dayNotesFormSubmit",
        type: "submit",
        name: "submit",
        form: "dayNotesForm"
    }));

    $dayNotesForm.append($("<input/>", {type: "hidden", name: "batch", value: "batchtemp"}));
    $dayNotesForm.append($("<input/>", {type: "hidden", name: "avis", value: "avisTemp"}));
    $dayNotesForm.append($("<input/>", {type: "hidden", name: "date", value: "2022-01-01"}));

    $dayNotesForm.submit(noteSubmitHandler);

    $primary.append($dayNotesForm);
    const $editionNav = $("<div/>", {class: 'btn-toolbar mb-2 mb-md-0'})
        .append($("<div/>", {
            class: 'btn-group mr-2 d-flex justify-content-evenly flex-wrap',
            id: 'edition-nav'
        }));
    $primary.append($editionNav);

    const editionShow = $("<div/>", {id: 'edition-show'}).append($("<h1>", {text: "show me a newspaper"}));
    $primary.append(editionShow);


    const mapKeys = Object.keys(currentEntities);
    for (let i = 0; i < mapKeys.length; i++) {
        const entity = mapKeys[i];
        if (i === currentEntityIndex) {
            let editionButton = $("<button/>", {
                class: "btn btn-sm btn-outline-secondary active",
                text: entity
            });
            $editionNav.append(editionButton);
        } else {
            const otherEditionLink = $("<a/>").attr({
                href: editEntityIndexInHash(location.hash, i),
                class: 'btn btn-sm btn-outline-secondary',
                text: entity
            });
            $editionNav.append(otherEditionLink);
        }
    }

    $("#edition-show").load("entityDisplay.html", function () {
        var mapKeys = Object.keys(currentEntities);
        var entity = currentEntities[mapKeys[currentEntityIndex]];
        if (entity.length === 1) {
            renderEntity(entity[0]);
        } else {
            renderSinglePagesEntity(entity, pageIndex);
        }

        let $editionNotesForm = $("#editionNotesForm");

        // alert($editionNotesForm.length) // if is == 0, not found form
        $editionNotesForm.append($("<input/>", {type: "hidden", name: "batch", value: "batchtemp"}));
        $editionNotesForm.append($("<input/>", {type: "hidden", name: "avis", value: "avisTemp"}));
        $editionNotesForm.append($("<input/>", {type: "hidden", name: "date", value: "2022-01-01"}));
        $editionNotesForm.append($("<input/>", {type: "hidden", name: "edition", value: "editionTemp"}));

        $editionNotesForm.submit(noteSubmitHandler);

    });


}

function renderEntity(entity) {
    let $pageDisplay = $("#pageDisplay");

    const date = moment(entity.editionDate).format("YYYY-MM-DD");

    let $pageForm = $("<form/>", {id: "pageNotesForm", action: "", method: "post"});
    $pageForm.append($("<label/>", {for: "pageNotes"}).text("Page notes"));
    $pageForm.append($("<textarea/>", {class: "userNotes", id: "pageNotes", type: "text", name: "notes"}));
    //TODO proper values for all fields
    $pageForm.append($("<input/>", {type: "hidden", name: "batch", value: "batchtemp"}));
    $pageForm.append($("<input/>", {type: "hidden", name: "avis", value: "avisTemp"}));
    $pageForm.append($("<input/>", {type: "hidden", name: "date", value: date}));
    $pageForm.append($("<input/>", {type: "hidden", name: "edition", value: entity.editionTitle}));
    $pageForm.append($("<input/>", {type: "hidden", name: "section", value: "SectionTemp"}));
    $pageForm.append($("<input/>", {type: "hidden", name: "page", value: entity.pageNumber}));
    $pageForm.append($("<input/>", {id: "pageNotesFormSubmit", type: "submit", name: "submit", form: "pageNotesForm"}));
    $pageForm.submit(noteSubmitHandler);

    $pageDisplay.append($pageForm);

    let value = "<div>Vis fil: " + entity.origRelpath + "<br> "
    if (entity.problems !== "") {
        value += "Problems: <pre>" + JSON.stringify(
            JSON.parse(entity.problems),
            ['type', 'filereference', 'description'],
            2) + "</pre>";
    }
    value += "</div>"
    $pageDisplay.append($(value));


    let infoHtml = "Edition titel: " + entity.editionTitle + "<br>";
    infoHtml += "Section titel: " + entity.sectionTitle + "<br>";
    infoHtml += "Side nummer: " + entity.pageNumber + "<br>";
    infoHtml += "Enkelt side: " + entity.singlePage + "<br>";
    infoHtml += "Afleverings dato: " + moment(entity.deliveryDate).format("YYYY-MM-DD") + "<br>";

    infoHtml += "Udgivelses dato: " + date + "<br>";
    infoHtml += "Format type: " + entity.formatType + "<br>";

    $("#medataDisplay").html(infoHtml);

}

function renderSinglePagesEntity(entity, page) {
    const $pageNav = $("#page-nav");
    for (var i = 0; i < entity.length; i++) {
        if (i === page) {
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

    renderEntity(entity[page]);
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

