var datesInYear;

function renderNewspaperForYear(newspaper, years, currentyear) {
    var nav = "<div class='btn-toolbar mb-2 mb-md-0'><div class='btn-group mr-2 d-flex justify-content-evenly flex-wrap' id='year-nav'></div></div>";

    let $primary = $("#primary-show");
    $primary.html(nav);
    $primary.append("<div id='year-show'><h1> show me a newspaper</h1></div>");

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
        datesInYear = splitDatesIntoMonths(dates);
        $("#year-show").load("calendarDisplay.html", function () {
            for (var i = 0; i < datesInYear.length; i++) {
                var calElem = "#month" + i;
                var html = "<h3>" + datesInYear[i].name + "</h3>";
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
        daysInMonth.push({day: moment(d), available: false, count: 0, problems: ""});
        d.add(1, 'days');
    }
    for (let i = 0; i < availableDates.length; i++) {
        let availableDate = availableDates[i];
        let element = daysInMonth[availableDate.day.date() - 1];
        element.available = true;
        element.count = availableDate.count;
        element.problems = availableDate.problems;
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

        let date = ("0"+dayInMonth.day.date());
        //Ensure same width of date numbers
        date = date.substring(date.length-2);

        calHtml += "<div class='col-sm-1' >"

        //Button styles: https://getbootstrap.com/docs/4.0/components/buttons/
        //primary = blue
        //secondary = dark grey
        //success = green
        //danger = red
        //warning = yellow
        //info = light blue
        //light = light grey
        //dark = black
        //link = link
        let btnClass = "class='btn btn-light btn-sm"
        if (dayInMonth.available) {
            let link = "#/newspapers/" + newspaper + "/" + dayInMonth.day.format('YYYY-MM-DD') + "/0/0/";

            calHtml += "<a style='padding-left: 0; padding-right: 0'";
            calHtml += "href='"+link+"' ";
            calHtml += "title='"+dayInMonth.count+" pages'";

            if (dayInMonth.problems.length > 0){
                btnClass += " btn-warning' "
            } else {
                btnClass += " btn-success' "
            }

            calHtml += btnClass + " > " + date + " </a>";
        } else {
            btnClass += " btn-light'"
            calHtml += "<button type='button' style='padding-left: 0; padding-right: 0' " +
                btnClass + " > " + date + " </button>";
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
        date[1] -= 1; //javascript uses 0-indexed months, so adapt
        let day = moment(date);
        months[day.month()].days.push({"day":day,"count":NewspaperDate.pageCount,"problems":NewspaperDate.problems});
    }
    return months;
}

function editYearIndexInHash(origHash, newIndex) {
    var hashParts = origHash.split("/");
    hashParts[hashParts.length - 2] = newIndex; // there's an empty place..
    let newHash = hashParts.join("/");
    return newHash;
}
