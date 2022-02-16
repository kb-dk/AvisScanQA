var datesInYear;

function renderNewspaperForYear(newspaper, years, currentyear) {
    var html;
    var nav = "<div class='btn-toolbar mb-2 mb-md-0'><div class='btn-group mr-2 d-flex justify-content-evenly flex-wrap' id='year-nav'></div></div>";

    $("#primary-show").html(nav);
    $("#primary-show").append("<div id='year-show'><h1> show me a newspaper</h1></div>");

    var currentLocationHash = location.hash;
    for (var i = 0; i < years.length; i++) {
        var year = years[i];
        if (year === currentyear) {
            $("#year-nav").append("<button class='btn btn-sm btn-outline-secondary active'>" + year + "</button>");
        } else {
            var newLocation = editYearIndexInHash(location.hash, year);
            $("#year-nav").append("<a href='" + newLocation + "' class='btn btn-sm btn-outline-secondary'>" + year + "</>");
        }
    }

    //See dk.kb.kula190.api.impl.DefaultApiServiceImpl.getDatesForNewspaperYear
    var url = 'api/dates/' + newspaper + '/' + currentyear;
    $.getJSON(url, {}, function (dates) {
        //TODO this should return more than just dates. A bit of metadata per date, such as the page count and if any errors occurred
        datesInYear = splitDatesIntoMonths(dates);
        $("#year-show").load("calendarDisplay.html", function () {
            for (var i = 0; i < datesInYear.length; i++) {
                var calElem = "#month" + i;
                var html = "<h3>Datoer i " + datesInYear[i].name + "</h3>";
                html += buildCalendar(currentyear, (i + 1), datesInYear[i].days, newspaper);
                $(calElem).html(html);
            }
        });
    });
}

function buildCalendar(year, month, availableDates, newspaper) {
	let firstDayOfThisMonth = moment(year + "-" + month + "-01", "YYYY-MM-DD");
	let daysInMonth = [];
	let firstWeekdayOfMonth = firstDayOfThisMonth.weekday();

    let d = moment(firstDayOfThisMonth);
    for (let i = 0; i < firstDayOfThisMonth.daysInMonth(); i++) {
        daysInMonth.push({day: moment(d), available: false, count: 0});
        d.add(1, 'days');
    }
    for (let i = 0; i < availableDates.length; i++) {
        let availableDate = availableDates[i];
        let element = daysInMonth[availableDate.day.date() - 1];
        element.available = true;
        element.count = availableDate.count;
    }

    var calHtml = "";

    if (firstWeekdayOfMonth > 0) {
        calHtml += "<div class='row'>";
        for (let i = 0; i < firstWeekdayOfMonth; i++) {
            calHtml += "<div class='col-sm-1'>&nbsp;</div>";
        }
    }

    for (let d = 0; d < daysInMonth.length; d++) {
        var colIdx = (firstWeekdayOfMonth + d) % 7;
        if (colIdx === 0) {
            if (d !== 0) {
                calHtml += "</div>";
            }
            calHtml += "<div class='row'>";
        }

        let dayInMonth = daysInMonth[d];

        let date = dayInMonth.day.date();

        calHtml += "<div class='col-sm-1' >"

        if (dayInMonth.available) {
            let link = "#/newspapers/" + newspaper + "/" + dayInMonth.day.format('YYYY-MM-DD') + "/0/0/";
            calHtml += "<a  title='"+dayInMonth.count+" pages' class='btn btn-success btn-sm' role='button' onmouseover='' href='"+link+"'>" + date + "</a>";
        } else {
            calHtml += "<button type='button' class='btn btn-light btn-sm'>" + date + "</button>";
        }
        calHtml += "</div>";
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

    let d;
    for (d in dates) {
        let NewspaperDate = dates[d]; //as [ 1920 , 1 ,2 ] with first month as 1
        let date = NewspaperDate.date;
        let count = NewspaperDate.pageCount
        date[1] -= 1; //javascript uses 0-indexed months, so adapt
        let day = moment(date);
        months[day.month()].days.push({"day":day,"count":count});
    }
    return months;
}

function editYearIndexInHash(origHash, newIndex) {
    var hashParts = origHash.split("/");
    hashParts[hashParts.length - 2] = newIndex; // there's an empty place..
    let newHash = hashParts.join("/");
    return newHash;
}
