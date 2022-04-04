let batchConfig;
$.getJSON("api/config.json", function (data) {
    batchConfig = data.batch;
})

function loadBatchForNewspaper(batchID) {
    let url = `api/batch/${batchID}`;

    let $state = $("#state-div").empty();
    const $headline = $("#headline-div").empty();
    let $notice = $("#notice-div").empty();

    $.getJSON(url)
        .fail(function (jqxhr, textStatus, error) {
            $headline.append($("<h1/>").text(`${jqxhr.responseText}`));
        })
        .done(function (batch) {
            let /*int*/ fromYEAR = moment(batch.startDate).format("YYYY");
            let /*int*/ toYEAR = moment(batch.endDate).format("YYYY");

            let currentNewspaperYear = fromYEAR;

            let newspaperYears = range(fromYEAR, toYEAR);

            $headline.append($("<h1/>").text(`Batch ${batch.batchid}`));

            $state.load("dropDownState.html", function () {
                let $stateDropDownMenu = $("#stateDropDownMenu");

                $("#stateFormBatchID").val(batch.batchid);
                const $stateForm = $("#stateForm");
                let $dropDownState = $("#dropDownState");
                $dropDownState.text(batchConfig.stateButtonOptions[batch.state].name)
                let stateButtonColors = batchConfig.stateButtonOptions[batch.state].styling;
                $dropDownState.css(stateButtonColors);

                for (let [option, val] of Object.entries(batchConfig.stateButtonOptions)) {
                    $stateDropDownMenu.append($("<button/>", {
                        type: "submit",
                        class: "dropdown-item",
                        title: val.description,
                        form: "stateForm",

                        value: `${option}`,
                        html: `${val.name}`,
                    }));

                }

                $(`[value="${batch.state}"`).css("font-weight", "Bold");

                $stateForm.submit(stateSubmitHandler);
                $state.append($stateForm);
            })

            if (batch.numNotes > 0) {
                $notice.append($("<a>", {
                    href: `#/batch/${batch.batchid}/notes`,
                    target: "_blank"
                }).text(`Get Notes (${batch.numNotes})`))
            }

            if (batch.numProblems > 0) {

                $notice.append($("<p>").text(`Total Problems found: ${batch.numProblems}`));

                if (batch.problems) {
                    console.log(batch.problems)
                    var output = JSON.parse(batch.problems).map(entry => JSON.stringify(entry, ['type', 'filereference', 'description'], 4).replaceAll("\\n","\n")).join(",\n")

                    $notice.append( $("<p>").text("Batch Problems:"))

                    $notice.append($("<pre/>",{id:"batchProblemsPre"}).text(output));

                }
            }
            let $notesButtonDiv = $("<div/>",{id:"notesButtonDiv"});
            let $notesButton = $("<button/>", {
                class: `notesButton btn ${batch.notes.length > 0 ? "btn-warning" : "btn-primary"}`,
                text: `${batch.notes.length > 0 ? "Show " + batch.notes.length + " notes and" : ""} create notes`
            });
            let $showNotesDiv = $("<div/>", {
                visible: false,
                class: `showNotesDiv ${(this.visible == 'true' ? "active" : "")}`,
                tabindex: "100"
            })
            setShowNotesFocusInAndOut($notesButton,$showNotesDiv);

            let $batchNotesForm = $("<form/>", {id: "batchNotesForm", action: "", method: "post"});
            const formRow1 = $("<div>", {class: "form-row"})
            const formRow2 = $("<div>", {class: "form-row"})
            $batchNotesForm.append(formRow1);
            $batchNotesForm.append(formRow2);

            formRow1.append($("<select/>", {
                class: "form-select", name: "standardNote"
            }).append($("<option>", {value: "", html: "", selected: "true"}))
                .append($("<option>", {
                    value: "Batch ugyldigt", html: "Batch ugyldigt"
                })))

            formRow2.append($("<textarea/>", {
                class: "userNotes", id: "batchNotes", type: "text", name: "notes"
            }));
            formRow2.append($("<input/>", {
                id: "batchNotesFormSubmit", type: "submit", name: "submit", form: "batchNotesForm", value:"Gem"
            }));

            $batchNotesForm.append($("<input/>", {type: "hidden", name: "batch", value: batch.batchid}));
            $batchNotesForm.append($("<input/>", {type: "hidden", name: "avis", value: batch.avisid}));

            $batchNotesForm.submit(noteSubmitHandler);
            $showNotesDiv.append($batchNotesForm);
            console.log(batch)
            if (batch.notes) {


                for (let i = 0; i < batch.notes.length; i++) {
                    // let $pageFormDiv = $("<div/>", {class: "pageFormDiv"});
                    let $batchForm = $("<form>", {action: "", method: "delete"});
                    $batchForm.append($("<input/>", {type: "hidden", name: "batch", value: batch.batchid}));

                    const note = batch.notes[i];
                    $batchForm.append($("<input/>", {type: "hidden", name: "id", value: note.id}));

                    const formRow = $("<div>", {class: "form-row"})
                    $batchForm.append(formRow);

                    let $batchNote = $("<textarea/>", {
                        class: "userNotes",
                        type: "text",
                        name: "notes",
                        text: note.note,
                        readOnly: "true",
                        disabled: true
                    });
                    formRow.append($("<label/>", {
                        for: $batchNote.uniqueId().attr("id"),
                        text: `-${note.username} ${moment(note.created).format("DD/MM/YYYY HH:mm:ss")}`
                    }))
                    formRow.append($batchNote);
                    formRow.append($("<button/>", {class: "bi bi-x-circle-fill", type: "submit"}).css({
                        "border": "none",
                        "background-color": "transparent"
                    }));
                    $batchForm.submit(noteDeleteHandler);
                    $showNotesDiv.append($batchForm);
                }
            }
            //let childrenOfNotesDiv = Array.from($showNotesDiv.childNodes);

            $notesButtonDiv.append($notesButton)
            $notice.append($notesButtonDiv);
            $notice.append($showNotesDiv);
            renderNewspaperForYear(newspaperYears, currentNewspaperYear, [url, currentNewspaperYear].join("/"));
            renderBatchTable(batch.avisid);

        });
}
function setShowNotesFocusInAndOut(focusInEl, focusOutEl){
    focusInEl.focusin((e) => {
        focusOutEl.addClass("active");
        focusOutEl.focus();
    });
    focusOutEl.focusout((e) => {
        let bool = !e.relatedTarget;//if relatedTarget is null bool is true
        if (bool){
            focusOutEl.removeClass("active");
        }else{
            bool = focusOutEl[0] != e.relatedTarget
            if (bool){
                if (!htmlElementWithinCollection(focusOutEl[0].children,e.relatedTarget.form)){
                    focusOutEl.removeClass("active");
                }
            }else{
                focusOutEl.focus();
            }
        }
    });
}
function htmlElementWithinCollection(collection,element){
    for(let i = 0; i < collection.length; i++){
        if(collection.item(i) == element){
            return true
        }
    }
    return false;
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
        filterControl: true,
        detailView: true,
        detailFormatter:function (i, r){
            console.log(r)
            return "State description: " + batchConfig.stateButtonOptions[r.state].description;
        },
        columns: [{
            title: 'BatchID',
            field: 'batchid',
            formatter: function (value, row) {
                // https://examples.bootstrap-table.com/index.html#column-options/formatter.html#view-source
                return "<a href='#/batch/" + value + "/'>" + value + "</a>";

            },
            sortable: true,
            filterControl: "input"
        }, {
            title: 'Delivery Date',
            field: 'deliveryDate',
            sortable: true,
            filterControl: "datepicker",
            "filterDatepickerOptions": {
                //Some of these appears to HAVE to be in " to be picked up...
                //https://bootstrap-datepicker.readthedocs.io/en/stable/options.html
                format: 'yyyy-mm-dd',
                autoclose: 'true',
                todayHighlight: 'true',
                "keyboardNavigation": 'true',
                "clearBtn": true,
                language: "da"
            }
        }, {
            title: 'State',
            field: 'state',
            sortable: true,
            filterControl: "select",
            searchFormatter: false, //Use this to have the select use the basic value, rather than the formatted value
            formatter: function (value, row) {
                // https://examples.bootstrap-table.com/index.html#column-options/formatter.html#view-source
                return "<div title='"+batchConfig.stateButtonOptions[value].description+"'>" + batchConfig.stateButtonOptions[value].name + "</div>";

            },
        }, {
            title: 'Problem Count',
            field: 'numProblems',
            sortable: true,
            filterControl: "input"
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
    }
    //console.log($(".stateCell").attributes("title", "test"))


}

function stateSubmitHandler(event) {
    event.preventDefault(); // <- cancel event

    const data = new FormData(event.target);
    const state = event.originalEvent.submitter.value;


    let parts = ["api", "batch", data.get('batch')]
    var query = new URLSearchParams();

    query.append("state", state);

    console.log(event.target);
    let url = parts.join("/") + "?" + query.toString();


    $.ajax({
        type: "POST",
        url: url,
        data: state,
        success: function () {
            alert("Batch has been accepted")
        },
        dataType: "json",
        contentType: "application/json"
    });

    location.reload(false);

    // alert('Handler for .submit() called.');
    return false;  // <- cancel event
}


function handleNotesDownload(batchId) {
    $.getJSON(`api/notes/${batchId}`)
        .done(function (notes) {
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
        });
}
