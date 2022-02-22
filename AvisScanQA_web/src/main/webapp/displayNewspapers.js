var datesInYear;

function renderNewspaperForYear(newspaper, years, currentyear) {
    var nav = "<div class='btn-toolbar mb-2 mb-md-0'><div class='btn-group mr-2 d-flex justify-content-evenly flex-wrap' id='year-nav'></div></div>";

    let $primary = $("#primary-show");
    $primary.html(nav);
    $primary.append("<div id='year-show'><h1> show me a newspaper</h1></div>");

    for (const year of years) {
        if (year == currentyear) { //== cannot be replaced with === as types differ....
            const button = $("<button/>", {
                class: 'btn btn-sm btn-outline-secondary active',
                text: year
            });
            $("#year-nav").append(button);
        } else {
            const link = $("<a/>", {
                href: editYearIndexInHash(location.hash, year),
                class: 'btn btn-sm btn-outline-secondary',
                text: year
            });
            $("#year-nav").append(link);
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


function renderBatchForYear(batch, years, currentyear) {
    const yearNav = $("<div/>", {
        class: 'btn-group mr-2 d-flex justify-content-evenly flex-wrap',
        id: 'year-nav'
    });
    var nav = $("<div/>", {
        class: 'btn-toolbar mb-2 mb-md-0'
    }).append(yearNav);


    let $primary = $("#primary-show");
    $primary.html(nav);

    const yearShow = $("<div/>", {id: 'year-show'})
    const heading = $("<h1/>", {text: "show me a newspaper"});
    yearShow.append(heading);
    $primary.append(yearShow);


    for (const year of years) {
        let thing;
        if (year === currentyear) {
            thing = $("<button/>", {
                class: 'btn btn-sm btn-outline-secondary active',
                text: year
            });
        } else {
            thing = $("<a/>", {
                href: editYearIndexInHash(location.hash, year),
                class: 'btn btn-sm btn-outline-secondary',
                text: year
            });
        }
        yearNav.append(thing);
    }

    //TODO
    //See dk.kb.kula190.api.impl.DefaultApiServiceImpl.getDatesForNewspaperYear
    var url = 'api/batch/' + batch + '/' + currentyear;
    $.getJSON(url, {}, function (dates) {
        datesInYear = splitDatesIntoMonths(dates);
        $("#year-show").load("calendarDisplay.html", function () {
            for (var i = 0; i < datesInYear.length; i++) {
                $("#month" + i).html("<h3>" + datesInYear[i].name + "</h3>" + buildCalendar(currentyear, (i + 1), datesInYear[i].days, batch));
            }
        });
    });
}

function determineColor(dayInMonth) {
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
    if (!dayInMonth.available) {
        return "btn-light";
    } else if (dayInMonth.state == "CHECKED"){ //TODO enum at some point
        return "approved";
    } else if (dayInMonth.problems.length > 0) {
        return "btn-warning";
    } else if (dayInMonth.count > 0) {
        return "btn-success";
    } else {
        return "btn-secondary";
    }

}

function buildCalendar(year, month, availableDates, newspaper) {

    let firstDayOfThisMonth = moment(year + "-" + month + "-01", "YYYY-MM-DD");
    let daysInMonth = [];
    let firstWeekdayOfMonth = firstDayOfThisMonth.weekday();

    let d = moment(firstDayOfThisMonth);
    for (let i = 0; i < firstDayOfThisMonth.daysInMonth(); i++) {
        daysInMonth.push({day: moment(d), available: false, count: 0, editionCount: 0, state: "", problems: ""});
        d.add(1, 'days');

    }
    for (let availableDate of availableDates) {

        let element = daysInMonth[availableDate.day.date() - 1];
        element.available = true;
        element.count = availableDate.count;
        element.editionCount = availableDate.editionCount;
        element.state = availableDate.state;
        element.problems = availableDate.problems;
    }

    let calHtml = "";
    calHtml += "<div class='row weekDayLetters'>";
    let weekDayLetters = ['M','T','O','T','F','L','S'];
    for (let i = 0; i <weekDayLetters.length; i++) {
        calHtml += "<div class='col-sm-1'>"+weekDayLetters[i]+"</div>";
    }
    calHtml += "</div>";
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

        let date = ("0" + dayInMonth.day.date());
        //Ensure same width of date numbers
        date = date.substring(date.length - 2);


        calHtml += "<div class='col-sm-1' >"

        let button;
        if (dayInMonth.available) {
            button = $("<a/>", {
                href: "#/newspapers/" + newspaper + "/" + dayInMonth.day.format('YYYY-MM-DD') + "/0/0/",
                title: dayInMonth.count + " page(s) \n" + dayInMonth.editionCount + " edition(s)"
            });
        } else {
            button = $("<button/>", {
                type: 'button'
            });
        }
        button.attr("style", 'padding-left: 0; padding-right: 0')
            .addClass("btn btn-sm " + determineColor(dayInMonth))
            .text(date);
        calHtml += button.prop('outerHTML');
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
        months[day.month()].days.push({
            "day": day,
            "count": NewspaperDate.pageCount,
            "editionCount": NewspaperDate.editionCount,
            "state": NewspaperDate.state,
            "problems": NewspaperDate.problems
        });
    }
    return months;
}

function editYearIndexInHash(origHash, newIndex) {
    var hashParts = origHash.split("/");
    hashParts[hashParts.length - 2] = newIndex; // there's an empty place..
    let newHash = hashParts.join("/");
    return newHash;
}
