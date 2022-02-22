function renderEntityDisplay(currentEntities, currentEntityIndex, pageIndex) {
    let curEntities = currentEntities;
    let curEntityIndex = currentEntityIndex;
    let curPageIndex = pageIndex;

    var nav = "<div class='btn-toolbar mb-2 mb-md-0'><div class='btn-group mr-2 d-flex justify-content-evenly flex-wrap' id='edition-nav'></div></div>";

    let $primary = $("#primary-show");

    $primary.html(nav);
    const editionShow = $("<div/>", {id: 'edition-show'}).append($("<h1>", {text: "show me a newspaper"}));

    $primary.append(editionShow);

    var mapKeys = Object.keys(currentEntities);
    const $edition = $("#edition-nav");

    for (var i = 0; i < mapKeys.length; i++) {
        var entity = mapKeys[i];
        if (i === currentEntityIndex) {
            let editionButton = $("<button/>",{
                class: "btn btn-sm btn-outline-secondary active",
                text: entity});
            $edition.append(editionButton);
        } else {
            const otherEditionLink = $("<a/>").attr({
                href: editEntityIndexInHash(location.hash, i),
                class: 'btn btn-sm btn-outline-secondary',
                text: entity});
            $edition.append(otherEditionLink);
        }
    }

    $("#edition-show").load("entityDisplay.html", function () {
        var mapKeys = Object.keys(curEntities);
        var entity = curEntities[mapKeys[curEntityIndex]];
        if (entity.length === 1) {
            renderEntity(entity[0]);
        } else {
            renderSinglePagesEntity(entity, curPageIndex);
        }
    });
}

function renderEntity(entity) {
    let value = "Vis fil: " + entity.origRelpath + "<br> "
    if (entity.problems !== "") {
        value += "Problems: <pre>" + JSON.stringify(
            JSON.parse(entity.problems),
            ['type', 'filereference', 'description'],
            2) + "</pre>";
    }
    $("#pageDisplay").html(value);

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
    for (var i = 0; i < entity.length; i++) {
        if (i === page) {
            const button = $("<button/>").attr({class: 'btn btn-sm btn-outline-secondary active'}).text(i + 1);
            $("#page-nav").append(button);
        } else {
            const link = $("<a/>").attr({
                href: editPageIndexInHash(location.hash, i),
                class: 'btn btn-sm btn-outline-secondary'
            }).text(i + 1);
            $("#page-nav").append(link);
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
