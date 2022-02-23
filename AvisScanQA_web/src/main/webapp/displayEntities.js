function renderEntityDisplay(currentEntities, currentEntityIndex, pageIndex) {
    let $primary = $("#primary-show");
    $primary.empty();
    let $dayNotesForm = $("<form>", {id: "dayNotesForm", action:"api/dayNotes",method:"post"});
    $dayNotesForm.append($("<label/>").text("Day notes").attr("for","dayNotes"));
    $dayNotesForm.append($("<input/>", {id: "dayNotes", type: "text",name:"dayNotes"}))
    $dayNotesForm.append($("<input/>", {id: "submit", type: "submit",name:"submit",form:"dayNotesForm"}));
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
    });
}

function renderEntity(entity) {
    let $pageDisplay = $("#pageDisplay");
    let $pageForm = $("<form/>", {id: "pageNotesForm", action:"api/pageNotes",method:"post"});
    $pageForm.append($("<label/>").text("Page notes").attr("for","pageNotes"));
    $pageForm.append($("<input/>", {id: "pageNotes", type: "text", name:"pageNotes"}));
    $pageForm.append($("<input/>", {id: "submit", type: "submit",name:"submit",form:"pageNotesForm"}));
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
    infoHtml += "Udgivelses dato: " + moment(entity.editionDate).format("YYYY-MM-DD") + "<br>";
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
