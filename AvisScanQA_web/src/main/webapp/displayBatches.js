

function loadBatchForNewspaper(batchID) {
    let url = `api/batch/${batchID}`;
    $.getJSON(url, {},
        function (batch) {
            let /*int*/ fromYEAR = moment(batch.startDate).format("YYYY");
            let /*int*/ toYEAR = moment(batch.endDate).format("YYYY");

            let currentNewspaperYear = fromYEAR;

            let newspaperYears = range(fromYEAR, toYEAR);

            const $headline = $("#headline-div");
            $headline.empty().append($("<h1/>").text(`Batch ${batch.batchid}`));
            let confirmForm = $("<form/>",{id:"confirmForm",action:"",method:"post"});
            confirmForm.append($("<input/>",{type: "hidden",name:"state",value:"APPROVED"}));
            confirmForm.append($("<input/>",{type:"submit",name:"submit",form:"confirmForm",class:"btn btn-success"}));
            confirmForm.submit(stateSubmitHandler);
            $headline.append(confirmForm)

            if (batch.numNotes > 0) {
                $headline.append($("<a>", {href: `#/batch/${batch.batchid}/notes`, target: "_blank"}).text(`Get Notes (${batch.numNotes})`))
            }

            if (batch.numProblems > 0) {
                let $notice = $("#notice-div").empty();

                $notice.append($("<p>").text(`Total Problems found: ${batch.numProblems}`));

                if (batch.problems) {
                    $notice
                        .append($("<p>").text("Batch Problems:")
                            .append("<pre/>").text(JSON.stringify(JSON.parse(batch.problems),
                                ['type', 'filereference', 'description'],
                                2)));
                }
            }

            renderNewspaperForYear(newspaperYears, currentNewspaperYear, [url, currentNewspaperYear].join("/"));
            renderBatchTable(batch.avisid);
        });
}


/**
 *
 * @param {int} start
 * @param {int} end
 * @returns {Generator<int, void, int>}
 */
function* range(start, end) {
    for (let i = start; i <= end; i++) {
        yield i;
    }
}


function renderBatchTable(filter) {
    let $table = $("#batchOverview-table");

    $table.bootstrapTable({
        url: "api/batch",
        filterControl:true,
        columns: [{
            title: 'BatchID',
            field: 'batchid',
            formatter: function (value, row) {
                // https://examples.bootstrap-table.com/index.html#column-options/formatter.html#view-source
                return "<a href='#/batch/" + value + "/'>" + value + "</a>";

            },
            sortable: true,
            filterControl:"input"
        }, {
            title: 'Delivery Date',
            field: 'deliveryDate',
            sortable: true,
            filterControl:"datepicker",
            "filterDatepickerOptions": {
                //Some of these appears to HAVE to be in " to be picked up...
                //https://bootstrap-datepicker.readthedocs.io/en/stable/options.html
                format: 'yyyy-mm-dd',
                autoclose: 'true',
                todayHighlight: 'true',
                "keyboardNavigation": 'true',
                "clearBtn":true,
                language: "da"
            }
        }, {
            title: 'State',
            field: 'state',
            sortable: true,
            filterControl:"select"
        }, {
            title: 'Problem Count',
            field: 'numProblems',
            sortable: true,
            filterControl:"input"
        }]
    })

    $table.bootstrapTable('refreshOptions', {
        filterOptions: {
            filterAlgorithm: 'or'
        }
    })

    if (filter != null) {
        //If we view a certain batch or newspaper, we only want batches from the same newspaper
        $table.bootstrapTable('filterBy', {
            avisid: filter
        })
    } else {
        $table.bootstrapTable('filterBy', {})
    }console.log($table.data());


}
function stateSubmitHandler(event) {
    event.preventDefault(); // <- cancel event

    const data = new FormData(event.target);
    console.log(data)
    let parts = ["api", "batch",data.get('batch')]
    var query = new URLSearchParams();

    query.append("avis", data.get('state'));

    // let url = parts.filter(x => x).join("/")
    let url = parts.join("/") + "?" + query.toString();

    console.log(url)
    const notes = data.get('state');

    $.ajax({
        type: "POST",
        url: url,
        data: notes,
        success: function () {
            alert("Batch has been accepted")
        },
        dataType: "json",
        contentType: "application/json"
    });

    // alert('Handler for .submit() called.');
    return false;  // <- cancel event
}


function handleNotesDownload(batchId) {
    $.getJSON(`api/notes/${batchId}`, {}, function (notes) {
        const items = notes;
        const replacer = (key, value) => value === null ? '' : value // specify how you want to handle null values here
        if (items.length > 0) {
            const header = Object.keys(items[0])
            const csv = [
                header.join(','), // header row first
                ...items.map(row => header.map(fieldName => JSON.stringify(row[fieldName], replacer)).join(','))
            ].join('\r\n')


            console.log(csv)
            let link = document.createElement("a");
            link.download = `${batchId}.csv`;
            link.href = `data:text/csv,${csv}`;
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            link.remove();
        }
        //Close the window, but wait 100 ms to ensure that the download have started
        setTimeout("window.close()", 100)
    })
}
