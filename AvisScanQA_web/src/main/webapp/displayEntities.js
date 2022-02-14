
var curEntities;
var curEntityIndex;
var curPageIndex;

function renderEntityDisplay(currentEntities, currentEntityIndex, pageIndex) {

	curEntities = currentEntities;
	curEntityIndex = currentEntityIndex;
	curPageIndex = pageIndex;
	
	var html;
	var nav = "<div class=\"btn-toolbar mb-2 mb-md-0\"><div class=\"btn-group mr-2 d-flex justify-content-evenly flex-wrap\" id=\"edition-nav\"></div></div>";

	$("#primary-show").html(nav);
	$("#primary-show").append("<div id=\"edition-show\"><h1> show me a newspaper</h1></div>");
	
	var mapKeys = Object.keys(currentEntities);
	var currentLocationHash = location.hash; 
	for(var i = 0; i < mapKeys.length; i++) {
		var entity = mapKeys[i];
		if(i == currentEntityIndex) {
			$("#edition-nav").append("<button class=\"btn btn-sm btn-outline-secondary active\">" + entity + "</button>");
        } else {
        	var newLocation = editEntityIndexInHash(location.hash, i);
        	$("#edition-nav").append("<a href=\"" + newLocation + "\" class=\"btn btn-sm btn-outline-secondary\">" + entity + "</>");
        }
	}
	
	$("#edition-show").load("entityDisplay.html", function() {
		var mapKeys = Object.keys(curEntities);
		var entity = curEntities[mapKeys[curEntityIndex]];
		if(entity.length == 1) {
			renderEntity(entity[0]);
		} else {
			renderSinglePagesEntity(entity, curPageIndex); 
		}
	});
}

function renderEntity(entity) {
	$("#pageDisplay").html("Vis fil: " + entity.origRelpath);
	
	var infoHtml = "Edition titel: " + entity.editionTitle + "<br>";
	infoHtml += "Section titel: " + entity.sectionTitle + "<br>";
	infoHtml += "Side nummer: " + entity.pageNumber + "<br>";
	infoHtml += "Enkelt side: " + entity.singlePage + "<br>";
	infoHtml += "Afleverings dato: " + moment(entity.deliveryDate).format("YYYY-MM-DD") + "<br>";
	infoHtml += "Udgivelses dato: " + moment(entity.editionDate).format("YYYY-MM-DD") + "<br>";
	infoHtml += "Format type: " + entity.formatType + "<br>";
	
	$("#medataDisplay").html(infoHtml);
	
	renderCharacterization(entity)
	renderImageContent(entity);
}

function renderSinglePagesEntity(entity, page) {
	for(var i=0; i<entity.length; i++) {
		if(i == page) {
			$("#page-nav").append("<button class=\"btn btn-sm btn-outline-secondary active\">" + (i+1) + "</button>");
        } else {
        	var newLocation = editPageIndexInHash(location.hash, i);
        	$("#page-nav").append("<a href=\"" + newLocation + "\" class=\"btn btn-sm btn-outline-secondary\">" + (i+1) + "</>");
        }
    }
	
	renderEntity(entity[page]);
}

function renderCharacterization(entity) {
	var url = 'web-qa/entity/' + entity.handle + "/characterization";
    $.get(url, function(content) {
    	$("#medataDisplay").append("<b> Characterisation:</b><br>")
    	for(var i=0; i<content.length; i++) {
    		$("#medataDisplay").append("Tool: " + content[i].tool + ": " + content[i].status + "<br>");
    	}
	});	
}

function renderImageContent(entity) {
	
	var format = entity.formatType.toLowerCase();
	switch(format) {
    case "pdf":
        displayPdf(entity, format);
        break;
    case "tiff":
    	displayTiff(entity, format);
        break;
    case "jp2":
    	displayJp2(entity, format);
        break;
    case "jpg":
    	displayJpeg(entity, format);
        break;
    default:
        alert("Kan på nuværrende tidspunkt ikke viser filer af typen " + entity.formatType.toLowerCase());
	}
}

function displayPdf(entity, format) {
	var url = 'web-qa/entity/' + entity.handle + "/url/" + format;
    $.get(url, function(contentUrl) {
		PDFObject.embed(contentUrl, "#pageDisplay");
	}, 'text');
}

function displayTiff(entity, format) {
	var url = 'web-qa/entity/' + entity.handle + "/url/" + format;
	$.get(url, {}, function(contentUrl) {
		var viewer = OpenSeadragon({
	        id: "pageDisplay",
	        prefixUrl: "openseadragon/images/",
	        tileSources: {
	            type: 'image',
	            url:  contentUrl
	        }
	    });
	}, 'text');
}

function displayJp2(entity, format) {
	var url = 'web-qa/entity/' + entity.handle + "/url/" + format;
	$.get(url, {}, function(contentUrl) {
		var viewer = OpenSeadragon({
	        id: "pageDisplay",
	        prefixUrl: "openseadragon/images/",
	        tileSources: contentUrl
	    });
	}, 'text');
}

function displayJpeg(entity, format) {
	var url = 'web-qa/entity/' + entity.handle + "/url/" + format;
	$.get(url, {}, function(contentUrl) {
		var viewer = OpenSeadragon({
	        id: "pageDisplay",
	        prefixUrl: "openseadragon/images/",
	        tileSources: {
	            type: 'image',
	            url:  contentUrl
	        }
	    });
	}, 'text');
}

function editEntityIndexInHash(origHash, newEntityIndex) {
	var hashParts = origHash.split("/");
	hashParts[hashParts.length-3] = newEntityIndex;
	var newHash = hashParts.join("/");
	return newHash;
}

function editPageIndexInHash(origHash, newPageIndex) {
	var hashParts = origHash.split("/");
	hashParts[hashParts.length-2] = newPageIndex; 
	var newHash = hashParts.join("/");
	return newHash;
}