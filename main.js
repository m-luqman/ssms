$(document).ready(function () {

    var loginUsnParam = "";
    
    const formStateName = 'data-form-state';
    
    var formStateEls = $('[' + formStateName + ']');
    
    const serverUrl = "https://ssms-app.herokuapp.com";
    
    function entryNameToCoachName(entryName) {
        return $('[name=' + entryName + ']').attr("coachname")
    }
    
    function optionNumberToOptionName(elementName, optionNumber) {
        if (elementName == "entryDifficulty")
            return $("label[for='" + $('[name=' + elementName + '][value=' + optionNumber + ']').attr('id') + "']").html();
    
        return $('[name=' + elementName + '] option[value=' + optionNumber + ']').text();
    }
    
    function getKey(anObject) {
        return Object.keys(anObject)[0];
    }
    function getValue(anObject) {
        return anObject[getKey(anObject)];
    }
    
    function diaryChecker() {
        let slides = document.getElementsByClassName("forethoughtEntry");
        for (let i = 0; i < slides.length; i++)
            if (!slides.item(i).validity.valid) {
                window.ConversationalForm.addRobotChatResponse("Some fields in forethought phase have not been filled");
                return false;
            }
    
        if (!$('#statusEntryForm')[0].checkValidity()) {
            window.ConversationalForm.addRobotChatResponse("Some fields have not been filled");
            return false;
        }
    
        return true;
    }
    
    function getDiaryData() {
        let diaryData = $('#statusEntryForm').serializeArray();
        $.merge(diaryData, $(".forethoughtEntry").serializeArray());
        $.merge(diaryData, $("#loginForm").serializeArray());
        return diaryData
    }
    
    function initializeConversation() {
        $("#statusEntryForm").conversationalForm({
            formEl: document.getElementById("statusEntryForm"),
            context: document.getElementById("statusEntryContainer"),
            hideUserInputOnNoneTextInput: true,
            preventAutoFocus: true,
            flowStepCallback: function (dto, success, error) {
                var currentStep = window.ConversationalForm.flowManager.getStep() + 1; // Steps are 0-based so we add 1
                var maxSteps = window.ConversationalForm.flowManager.maxSteps; // This value is not 0-based
                if (currentStep == maxSteps) {
    
                    if (!diaryChecker()) {
                        error();
                        return;
                    }
    
                    let action = serverUrl + "/coach";
                    let diaryData = getDiaryData();
    
                    $.ajax({
                        url: action,
                        method: 'POST',
                        processData: false,
                        data: $.param(diaryData)
                    }).done(function (data) {
    
                        if (data.length == 0) {
                            success();
                            return;
                        }
    
                        let children = data.map(val => ({
                            "tag": "option",
                            "cf-label": entryNameToCoachName(getKey(val)),
                            "value": getKey(val)
                        }));
    
                        let tags = [
                            {
                                // select group
                                "id": "entryCoach",
                                "tag": "select",
                                "cf-questions": "Select one of the options below to get suggestions on how you could make your study more efficient.",
                                "name": "coach-options",
                                "isMultiChoice": false,
                                "children": children
                            },
                            {
                                "tag": "cf-robot-message",
                                "name": "coach-options",
                                "cf-questions": "Try focusing on one of the options below when repeating the study of this topic or when studying similar topics."
                            }
                        ];
    
                        let conditionalTags = data.flatMap(val => (getValue(val)
                            .map(op => (
                                {
                                    "tag": "cf-robot-message",
                                    "cf-questions": optionNumberToOptionName(getKey(val), op).trim(),
                                    "cf-conditional-coach-options": getKey(val)
                                }
                            ))));
    
                        window.ConversationalForm.addTags(tags.concat(conditionalTags));
                        success();
    
                    }).fail(function (data) {
                        window.ConversationalForm.addRobotChatResponse("There was an error in providing feedback");
                        error();
                    });
                }
                else {
                    success();
                }
            },
            submitCallback: function () {
    
                if (!diaryChecker()) {
                    error();
                    return;
                }
    
                let action = serverUrl + $('#statusEntryForm').attr('action');
                let diaryData = getDiaryData();
                diaryData.push({ name: "entryDateTime", value: moment().format('YYYY-MM-DD H:mm:ss') });
    
                $.ajax({
                    url: action,
                    method: 'POST',
                    processData: false,
                    data: $.param(diaryData)
                }).done(function (data) {
                    window.ConversationalForm.addRobotChatResponse("Diary successfully entered");
                }).fail(function (data) {
                    window.ConversationalForm.addRobotChatResponse("There was an error entering the diary. Please try again.");
                });
            }
        });
    }
    
    async function fillCombo(comboId, formId, path, fieldName) {
        let combo = document.getElementById(comboId);
        let selected = $(combo).val();
        return $.getJSON(serverUrl + path + $('#' + formId).serialize(), function (data) {
            combo.value = "";
            combo.options.length = 1;
            if (selected != "" && selected != "unselected") {
                i = data.findIndex(x => x[fieldName] == selected)
                if (i == -1) {
                    combo.add(new Option(selected));
                }
            }
            for (index in data) {
                combo.add(new Option(data[index][fieldName]));
            }
            $(combo).val(selected);
        });
    }
    
    function fillFilterCombos() {
        fillCombo('subject', 'filter-form', '/fill/subjects?', 'Subject_Name');
        fillCombo('usn', 'filter-form', '/fill/usn?', 'USN');
    }
    
    function fillEntryCombos() {
        fillCombo('entryTextbook', 'syllabusEntryForm', '/fill/textbooks?', 'Textbook_Name');
        fillCombo('entrySubject', 'syllabusEntryForm', '/fill/subjects?', 'Subject_Name');
    }
    
    function fillDailyTopicCountCombo() {
        fillCombo('time-usn', 'time-popover-form', '/fill/usn?', 'USN');
    }
    
    function loadTable() {
        $('#table').bootstrapTable('refresh', { url: serverUrl + "/filter?" + $('#filter-form').serialize() });
    }
    
    function filterUsing(element, reactTo = function (e) { }) {
        $(element).change(function () {
            reactTo(event);
            loadTable();
        })
    }
    
    function logout() {
        $("#loginForm").show("slow");
        $("#entryDiv").hide("slow");
    }
    
        $('#refreshConversation').click(function () {
            initializeConversation();
        });
    
        $('[data-toggle="popover"]').popover(
            {
                placement: 'top',
                html: true
            }
        );
    
        $('#time-popover').popover(
            {
                placement: 'top',
                html: true,
                content: function () {
                    return $("#time-popover-content").html();
                },
                title: "How many topics to study today?"
            }
        );
    
        $('#fullpage').fullpage({
            //options here
            loopBottom: false,
            fitToSection: false,
            autoScrolling: false,
            scrollOverflow: false,
            scrollBar: true,
            navigation: false,
            navigationPosition: 'top',
            navigationTooltips: [
                'Forethought phase',
                'Goal setting',
                'Strategic planning',
                'Assesment criteria',
                'Performance level',
                'Afterthought phase'
            ],
            licenseKey: "40E19E81-306A46E3-85506FDC-25A03E3A"
        });
    
        $('#table').on('check.bs.table', function (e, row) {
            $('.entryTopic').find('option').remove();
            $('.entryTopic').append(new Option(row['Topic Name'], row['Topic Name'], true, true));
        });
    
        $('#table').on('uncheck.bs.table', function (e, row) {
            $('.entryTopic').find('option').remove();
            $('.entryTopic').append(new Option("please select a topic in the forethought phase", "", true, true));
        });
    
        $('#simple-menu').sidr({
            side: "right",
            onOpenEnd: function () { $('html').css('overflow', 'hidden'); },
            onCloseEnd: function () { $('html').css('overflow', 'auto'); }
        });
    
        $('#goalSetting').swipe({
            //Single swipe handler for left swipes
            swipeLeft: function () {
                $.sidr('open');
            },
            swipeRight: function () {
                $.sidr('close');
            },
            //Default is 75px, set to 0 for demo so any distance triggers swipe
            threshold: 45,
            excludedElements: "a, thead, button, input, select, textarea"
        });
    
        flatpickr(".flatpickr-single", { wrap: true, dateFormat: "Y-m-d" });
    
        flatpickr
            (".flatpickr-multi"
                , {
                    wrap: true,
                    mode: "range",
                    dateFormat: "Y-m-d"
                }
            );
    
        $(".filters:not('.no-change')").on('change', fillFilterCombos);
    
        $(".entries:not('.no-change')").on('change', fillEntryCombos);
    
        $('#filter-btn').on('click', loadTable);
    
        $('#loginForm').submit(function (e) {
            e.preventDefault();
            var form = $(this);
            var action = serverUrl + "/login";
            var formData = form.serialize();
            $.ajax({
                url: action,
                method: 'POST',
                processData: false,
                data: formData,
            }).done(function (data) {
                loginUsnParam = $.param({ "loginUsn": $("#loginUsn").val() });
                formStateEls.hide();
                if (data == "admin") {
                    $(".adminComponent").show("slow");
                }
                else {
                    $(".adminComponent").hide();
                }
                $("#loginForm").hide("slow");
                $("#entryDiv").show("slow");
                initializeConversation();
            }).fail(function (data) {
                loginUsnParam = "";
                formStateEls.hide();
                form.find('[' + formStateName + '=loginError]').stop(1).fadeIn().delay(3000).fadeOut();
            });
        });
    
        $('.entryForm').submit(function (e) {
            e.preventDefault();
            var form = $(this);
            var action = serverUrl + form.attr('action');
            var formData =
                form.serialize()
                + "&" + loginUsnParam
                + "&" + $.param({ "entryDateTime": moment().format('YYYY-MM-DD H:mm:ss') });
    
            $.ajax({
                url: action,
                method: 'POST',
                processData: false,
                data: formData,
            }).done(function (data) {
                formStateEls.hide();
                form.find('[' + formStateName + '=entrySuccess]').stop(1).fadeIn().delay(3000).fadeOut();
            }).fail(function (data) {
                formStateEls.hide();
                form.find('[' + formStateName + '=entryError]').stop(1).fadeIn().delay(3000).fadeOut();
            });
    
        });
        $('#registrationForm').submit(function (e) {
            e.preventDefault();
            var form = $(this);
            var action = serverUrl + "/register";
            var formData = form.serialize();
            $.ajax({
                url: action,
                method: 'POST',
                processData: false,
                data: formData,
            }).done(function (data) {
                formStateEls.hide();
                form.find('[' + formStateName + '=registrationSuccess]').stop(1).fadeIn().delay(3000).fadeOut();
            }).fail(function (data) {
                formStateEls.hide();
                form.find('[' + formStateName + '=registrationError]').stop(1).fadeIn().delay(3000).fadeOut();
            });
    
        });

        document.querySelector('body').addEventListener('submit', function(e) {
            if (e.target.id === 'time-popover-form') {
                e.preventDefault();
                var form = $(this);
                var action = serverUrl + "/dailyTopicCount";
                var formData = form.serialize();
                $.ajax({
                    url: action,
                    method: 'POST',
                    processData: false,
                    data: formData
                }).done(function (data) {
                    $('#daily-topic-count').text(data)
                    $('#daily-topic-count-alert').show();
                }).fail(function (data) {
                    alert("failure");
                    $('#daily-topic-count-alert').hide();
                });
            }
          });

        async function loadInitialData(){  
            await Promise.all([fillFilterCombos(), fillEntryCombos(), fillDailyTopicCountCombo()]);
        };

        loadInitialData();

        $('#daily-topic-count-alert').hide();

        filterUsing('.filters');
    
        filterUsing(
            '#usn'
            , function (event) {
                if (event.target.value == "unselected" || event.target.value == "") {
                    $('#table').bootstrapTable('showColumn', 'Total Students (%)');
                    $('#table').bootstrapTable('hideColumn', 'Obtained Marks (%)');
                }
                else {
                    $('#table').bootstrapTable('hideColumn', 'Total Students (%)');
                    $('#table').bootstrapTable('showColumn', 'Obtained Marks (%)');
                }
            }
        );
    });
    