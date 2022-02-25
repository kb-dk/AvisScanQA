

function renderBatchTable(filter) {
    let $table = $("#batchOverview-table");

    $table.bootstrapTable({
        url: "api/batch",
        filterControl:true,
        columns: [{
            title: 'BatchID',
            field: 'batchid',
            formatter: linkFormatter,
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
    }
    // $table.show();
    console.log($table.data());


}


function linkFormatter(value, row) {
    // https://examples.bootstrap-table.com/index.html#column-options/formatter.html#view-source
    return "<a href='#/batch/" + value + "/'>" + value + "</a>";

}
