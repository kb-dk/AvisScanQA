
var datesInYear;

function renderNewspaperForYear(newspaper, years, currentyear) {
	var html;
	var nav = "<div class=\"btn-toolbar mb-2 mb-md-0\"><div class=\"btn-group mr-2 d-flex justify-content-evenly flex-wrap\" id=\"year-nav\"></div></div>";

	$("#primary-show").html(nav);
	$("#primary-show").append("<div id=\"year-show\"><h1> show me a newspaper</h1></div>");

	var currentLocationHash = location.hash;
	for(var i = 0; i < years.length; i++) {
		var year = years[i];
		if(year === currentyear) {
			$("#year-nav").append("<button class=\"btn btn-sm btn-outline-secondary active\">" + year + "</button>");
        } else {
        	var newLocation = editYearIndexInHash(location.hash, year);
        	$("#year-nav").append("<a href=\"" + newLocation + "\" class=\"btn btn-sm btn-outline-secondary\">" + year + "</>");
        }
	}

	var url = 'api/dates/' + newspaper + '/' + currentyear;
	$.getJSON(url, {}, function(dates) {
		datesInYear = splitDatesIntoMonths(dates);
		$("#year-show").load("calendarDisplay.html", function() {
			for(var i = 0; i < datesInYear.length; i++) {
				var calElem = "#month"+i;
				var html = "<h3>Datoer i " + datesInYear[i].name + "</h3>";
				html += buildCalendar(currentyear, (i+1), datesInYear[i].days, newspaper);
				$(calElem).html(html);
			}
        });
    });
}

function buildCalendar(year, month, availableDates, newspaper) {
	var firstDayOfThisMonth = moment(year + "-" + month + "-01", "YYYY-MM-DD");
	var daysInMonth = [];
	var firstWeekdayOfMonth =  firstDayOfThisMonth.weekday();

	var d = moment(firstDayOfThisMonth);
	for(var i = 0; i<firstDayOfThisMonth.daysInMonth(); i++) {
		daysInMonth.push({day: moment(d), available: false});
		d.add(1, 'days');
	}
	for(var i = 0; i<availableDates.length; i++) {
		daysInMonth[availableDates[i].date()-1].available = true;
	}

	var calHtml = "";

	if(firstWeekdayOfMonth > 0) {
		calHtml += "<div class=\"row\">";
		for(var i = 0; i<firstWeekdayOfMonth; i++) {
			calHtml += "<div class=\"col-sm-1\">&nbsp;</div>";
		}
	}

	for(var d = 0; d<daysInMonth.length; d++) {
		var colIdx = (firstWeekdayOfMonth + d) % 7;
		if(colIdx === 0) {
			if(d === 0) {
				calHtml += "<div class=\"row\">";
			} else {
				calHtml += "</div><div class=\"row\">";
			}
		}
		if(daysInMonth[d].available) {
			calHtml += "<div class=\"col-sm-1\"><a class=\"btn btn-success btn-sm\" role=\"button\""
				+ " href=\"#/newspapers/" + newspaper + "/" + daysInMonth[d].day.format('YYYY-MM-DD') +"/0/0/\">" + daysInMonth[d].day.date() + "</a></div>";
		} else {
			calHtml += "<div class=\"col-sm-1\"><button type=\"button\" class=\"btn btn-light btn-sm\">" + daysInMonth[d].day.date() + "</button></div>";
		}
	}

	calHtml += "</div>";

	return calHtml;
}


function splitDatesIntoMonths(dates) {
	var months = [];
	months[0] = {name: "Januar", days: []};
	months[1] = {name: "Februar", days: []};
	months[2] = {name: "Marts", days: []};
	months[3] = {name: "April", days: []};
	months[4] = {name: "Maj", days: []};
	months[5] = {name: "Juni", days: []};
	months[6] = {name: "Juli", days: []};
	months[7] = {name: "August", days: []};
	months[8] = {name: "September", days: []};
	months[9] = {name: "Oktober", days: []};
	months[10] = {name: "November", days: []};
	months[11] = {name: "December", days: []};

	var d;
	for(d in dates) {
		day = moment(dates[d]);
		months[day.month()].days.push(day);
    }
	return months;
}

function editYearIndexInHash(origHash, newIndex) {
	var hashParts = origHash.split("/");
	hashParts[hashParts.length-2] = newIndex; // there's an empty place..
	var newHash = hashParts.join("/");
	return newHash;
}
