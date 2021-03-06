$(document).ready(function () {
  var loginUsnParam = "";

  const formStateName = "data-form-state";

  var formStateEls = $("[" + formStateName + "]");

  const serverUrl = "https://ssms-app.herokuapp.com";

  var new_topic_count_array = [];

  var forethought_coach_data = [];

  var formSelectors = [];

  var forethoughtConversationElement = null;

  var unselectedOption = {
    tag: "option",
    "cf-label": "unselected",
    value: "",
  };

  const afterthoughtConversationJson = {
    formEl: document.getElementById("statusEntryForm"),
    context: document.getElementById("statusEntryContainer"),
    hideUserInputOnNoneTextInput: true,
    preventAutoFocus: true,
    loadExternalStyleSheet: false,
    submitCallback: function (form) {

      let serializer = new FormSerializer()
        .withElementsFromSelectors(formSelectors)
        .withElement(form.formEl);

      serializer =
        forethoughtConversationElement != null
          ? serializer.withElement(forethoughtConversationElement.formEl)
          : serializer;

      let action = serverUrl + $("#statusEntryForm").attr("action");
      let diaryData = serializer.getSerializedArray().concat([
        {
          name: "entryDateTime",
          value: moment().format("YYYY-MM-DD H:mm:ss"),
        },
      ]);

      $.ajax({
        url: action,
        method: "POST",
        processData: false,
        data: $.param(diaryData),
      })
        .done(function (data) {
          form.addRobotChatResponse("Diary successfully entered");
          if (new_topic_count_array.length)
            updateDailyTopicCounts(new_topic_count_array);
          loadTable();
        })
        .fail(function (data) {
          form.addRobotChatResponse(
            "There was an error entering the diary. Please try again."
          );
        });
    },
    flowStepCallback: function (dto, success, error) {
      var currentStep = this.cfReference.flowManager.getStep() + 1; // Steps are 0-based so we add 1
      var maxSteps = this.cfReference.flowManager.maxSteps; // This value is not 0-based

      if (!dto.tag.domElement.checkValidity()) {
        return error();
      }

      if (currentStep !== maxSteps) {
        return success();
      }

      if (dto.tag.name != null && dto.tag.name.startsWith("coach")) {
        return success();
      }

      let serializer = new FormSerializer()
        .withElementsFromSelectors(formSelectors)
        .withElement(this.cfReference.formEl);

      serializer =
        forethoughtConversationElement != null
          ? serializer.withElement(forethoughtConversationElement.formEl)
          : serializer;

      if (!serializer.isValidData()) {
        this.cfReference.addRobotChatResponse(
          "Some fields have not been filled"
        );
        return error();
      }

      let action = serverUrl + "/coach_afterthought";
      let diaryData = serializer.getSerializedArray();
      let that = this;
      $.ajax({
        url: action,
        method: "POST",
        processData: false,
        data: $.param(diaryData),
      })
        .done(function (data) {
          if (data.length == 0) {
            return success();
          }

          that.cfReference.addTags(
            [
              {
                // select group
                id: "entryCoach",
                tag: "select",
                required: true,
                "cf-questions":
                  "Select the aspect of your study you think you need to adjust",
                name: "coach-options",
                isMultiChoice: false,
                children: data.map((val) => ({
                  tag: "option",
                  "cf-label": entryNameToCoachName(getKey(val)),
                  value: getKey(val),
                })),
              },
              {
                tag: "cf-robot-message",
                name: "coach-options",
                "cf-questions":
                  "Reflect upon how you could have better studied this topic within time, if you had chosen one of the following",
              },
            ].concat(
              data.flatMap((val) => ({
                tag: "cf-robot-message",
                "cf-conditional-coach-options": getKey(val),
                "cf-questions": getValue(val)
                  .map((op) => optionNumberToOptionName(getKey(val), op).trim())
                  .join("&&"),
              }))
            )
          );
          return success();
        })
        .fail(function (data) {
          that.cfReference.addRobotChatResponse(
            "There was an error in providing feedback"
          );
          return error();
        });
    },
  };

  const forethoughtConversationJson = (data) => ({
    options: {
      context: document.getElementById("coach-revision-body"),
      hideUserInputOnNoneTextInput: true,
      preventAutoFocus: true,
      loadExternalStyleSheet: false,
      submitCallback: function (form) {
        [...form.formEl.elements]
          .filter((element) => element.value === "")
          .forEach((element) => $(element).remove());
      },
      flowStepCallback: function (dto, success, error) {
        if (!dto.tag.domElement.checkValidity()) {
          return error();
        }
        return success();
      },
    },
    tags: [
      {
        tag: "cf-robot-message",
        "cf-questions":
          "The following are the study choices you made for this topic, last time",
      },
    ]
      .concat(
        data.past_choices.map((val) => ({
          tag: "select",
          required: true,
          name: "previous_" + getKey(val),
          "cf-questions": entryNameToCoachName(getKey(val)),
          children: getValue(val).flatMap((op) => ({
            tag: "option",
            value: op,
            "cf-label": optionNumberToOptionName(getKey(val), op).trim(),
          })),
        }))
      )
      .concat([
        {
          tag: "select",
          name: "isSuggestionNeeded",
          "cf-questions": "Do you want to make changes to an aspect of your prior study?",
          children: [
            {
              tag: "option",
              "cf-label": "yes",
              value: "yes"
            },
            {
              tag: "option",
              "cf-label": "no",
              value: "no"
            }
          ]
        },
        {
          // select group
          tag: "select",
          required: true,
          "cf-questions":
            "Select one aspect of your prior study that you want to change, this time",
          name: "entryRevisionFocus",
          "cf-conditional-isSuggestionNeeded": "yes",
          isMultiChoice: false,
          children: [unselectedOption].concat(
            data.recommended_choices.map((val) => ({
              tag: "option",
              "cf-label": entryNameToCoachName(getKey(val)),
              value: getKey(val),
            }))
          ),
        },
      ])
      .concat(
        data.recommended_choices.map((val) => ({
          tag: "select",
          name: "coach_" + getKey(val),
          required: true,
          "cf-conditional-isSuggestionNeeded": "yes",
          "cf-questions":
            "Select the change that you think would help you better study this topic within time",
          "cf-conditional-entryRevisionFocus": getKey(val),
          children: [unselectedOption].concat(
            getValue(val).map((op) => ({
              tag: "option",
              "cf-label": optionNumberToOptionName(getKey(val), op).trim(),
              value: op,
            }))
          ),
        }))
      ),
  });

  function entryNameToCoachName(entryName) {
    return $("[name=" + entryName + "]").attr("coachname");
  }

  function optionNumberToOptionName(elementName, optionNumber) {
    if (elementName == "entryDifficulty")
      return $(
        "label[for='" +
          $("[name=" + elementName + "][value=" + optionNumber + "]").attr(
            "id"
          ) +
          "']"
      ).html();

    return $(
      "[name=" + elementName + "] option[value=" + optionNumber + "]"
    ).text();
  }

  function getKey(anObject) {
    return Object.keys(anObject)[0];
  }
  function getValue(anObject) {
    return anObject[getKey(anObject)];
  }

  function FormSerializer(elements = []) {
    this.formElements = elements;
    this.withElementsFromSelectors = (selectors) =>
      new FormSerializer(
        this.formElements.concat($(selectors.join(", ")).toArray())
      );
    this.withElement = (element) =>
      element == null
        ? this
        : new FormSerializer(this.formElements.concat([element]));
    this.getSerializedArray = () =>
      this.formElements
        .map((element) => $(element).serializeArray())
        .reduce((prev, curr) => prev.concat(curr));
    this.isValidData = () =>
      this.formElements.every((element) => element.checkValidity());
  }

  function initializeConversation() {
    $("#statusEntryForm").conversationalForm(afterthoughtConversationJson);
  }

  async function fillCombo(comboId, formId, path, fieldName) {
    let combo = document.getElementById(comboId);
    let selected = $(combo).val();
    $(combo).css("cursor", "progress");
    return $.getJSON(serverUrl + path + $("#" + formId).serialize(), function (
      data
    ) {
      combo.value = "";
      combo.options.length = 1;
      if (selected != "" && selected != "unselected") {
        i = data.findIndex((x) => x[fieldName] == selected);
        if (i == -1) {
          combo.add(new Option(selected));
        }
      }
      for (index in data) {
        combo.add(new Option(data[index][fieldName]));
      }
      $(combo).val(selected);
      $(combo).css("cursor", "default");
    });
  }

  function fillFilterCombos() {
    fillCombo("subject", "filter-form", "/fill/subjects?", "Subject_Name");
    fillCombo("usn", "filter-form", "/fill/usn?", "USN");
  }

  function fillEntryCombos() {
    fillCombo(
      "entryTextbook",
      "syllabusEntryForm",
      "/fill/textbooks?",
      "Textbook_Name"
    );
    fillCombo(
      "entrySubject",
      "syllabusEntryForm",
      "/fill/subjects?",
      "Subject_Name"
    );
  }

  function fillDailyTopicCountCombo() {
    fillCombo("time-usn", "time-popover-form", "/fill/usn?", "USN");
  }

  function loadTable() {
    $("#table").bootstrapTable("refresh", {
      url: serverUrl + "/filter?" + $("#filter-form").serialize(),
    });
  }

  function filterUsing(element, reactTo = function (e) {}) {
    $(element).change(function () {
      reactTo(event);
      loadTable();
    });
  }

  function logout() {
    $("#loginForm").show("slow");
    $("#entryDiv").hide("slow");
  }

  $("#refreshAfterthoughtConversation").click(function () {
    initializeConversation();
  });
  $("#refreshForethoughtConversation").click(function () {
    if (forethought_coach_data) whenForethoughtCoach(forethought_coach_data);
  });

  $('[data-toggle="popover"]').popover({
    placement: "top",
    html: true,
  });

  $("#time-popover").popover({
    placement: "top",
    html: true,
    container: "body",
    sanitize: false,
    content: () => $("#time-popover-content").html(),
    title: "How many topics to study today?",
  });

  $("#fullpage").fullpage({
    //options here
    paddingTop: "50px",
    paddingBottom: "50px",
    loopBottom: false,
    fitToSection: false,
    autoScrolling: false,
    scrollOverflow: false,
    scrollBar: true,
    navigation: false,
    navigationPosition: "top",
    navigationTooltips: [
      "Forethought phase",
      "Goal setting",
      "Strategic planning",
      "Assesment criteria",
      "Performance level",
      "Afterthought phase",
    ],
    onLeave: function (origin, destination, direction) {
      if (this.item.contains(document.getElementById("collapseStatusEntry"))) {
        $("#collapseStatusEntry").collapse("hide");
      }
      if (
        destination.item.contains(
          document.getElementById("collapseStatusEntry")
        )
      ) {
        $("#collapseStatusEntry").collapse("show");
      }
      if (
        this.item.contains(
          document.getElementById("collapse-coach-revision-body")
        )
      ) {
        $("#collapse-coach-revision-body").collapse("hide");
      }
      if (
        destination.item.contains(
          document.getElementById("collapse-coach-revision-body")
        )
      ) {
        $("#collapse-coach-revision-body").collapse("show");
      }
    },
    licenseKey: "40E19E81-306A46E3-85506FDC-25A03E3A",
  });

  function forethoughtCoachStateTransition(
    whenForethoughtCoach,
    whenNotForethoughtCoach
  ) {
    $("#table").on("check.bs.table", function (e, row) {
      $(".entryTopic").find("option").remove();
      $(".entryTopic").append(
        new Option(row["Topic Name"], row["Topic Name"], true, true)
      );
      if (
        !$("#newOldToggle").is(":checked") &&
        $("#usn").val() != "unselected"
      ) {
        let dataArray = [
          {
            name: "usn",
            value: $("#usn").val(),
          },
          {
            name: "entryTopic",
            value: row["Topic Name"],
          },
        ];
        $.ajax({
          url: serverUrl + "/coach_forethought",
          method: "POST",
          processData: false,
          data: $.param(dataArray),
        })
          .done(function (data) {
            if (data.recommended_choices.length == 0) {
              whenNotForethoughtCoach();
              return;
            }
            forethought_coach_data = data;
            whenForethoughtCoach(data);
          })
          .fail(function (data) {
            alert("error");
          });
      } else {
        whenNotForethoughtCoach();
      }
    });

    $("#table").on("uncheck.bs.table", function (e, row) {
      $(".entryTopic").find("option").remove();
      $(".entryTopic").append(
        new Option(
          "please select a topic in the forethought phase",
          "",
          true,
          true
        )
      );
      whenNotForethoughtCoach();
    });

    $("#collapse-coach-revision-body").on("show.bs.collapse", function () {
      $("#coach-revision-body").css("min-height", "500px");
    });
  }
  $("#simple-menu").sidr({
    side: "right",
    onOpenEnd: function () {
      $("html").css("overflow", "hidden");
    },
    onCloseEnd: function () {
      $("html").css("overflow", "auto");
    },
  });

  $("#goalSetting").swipe({
    //Single swipe handler for left swipes
    swipeLeft: function () {
      $.sidr("open");
    },
    swipeRight: function () {
      $.sidr("close");
    },
    //Default is 75px, set to 0 for demo so any distance triggers swipe
    threshold: 45,
    excludedElements: "a, thead, button, input, select, textarea",
  });

  flatpickr(".flatpickr-single", { wrap: true, dateFormat: "Y-m-d" });

  flatpickr(".flatpickr-multi", {
    wrap: true,
    mode: "range",
    dateFormat: "Y-m-d",
  });

  $(".filters:not('.no-change')").on("change", fillFilterCombos);

  $(".entries:not('.no-change')").on("change", fillEntryCombos);

  $("#filter-btn").on("click", loadTable);

  $("#logout").on("click", logout);

  $(".hide-parent").on("click", () => $(".hide-parent").parent().hide("slow"));

  $("#daily-topic-count-close-btn").on(
    "click",
    () => (new_topic_count_array = [])
  );

  $("#loginForm").submit(function (e) {
    e.preventDefault();
    var form = $(this);
    var action = serverUrl + "/login";
    var formData = form.serialize();
    $.ajax({
      url: action,
      method: "POST",
      processData: false,
      data: formData,
    })
      .done(function (data) {
        loginUsnParam = $.param({ loginUsn: $("#loginUsn").val() });
        formStateEls.hide();
        if (data == "admin") {
          $(".adminComponent").show("slow");
        } else {
          $(".adminComponent").hide();
        }
        $("#loginForm").hide("slow");
        $("#entryDiv").show("slow");
        initializeConversation();
      })
      .fail(function (data) {
        loginUsnParam = "";
        formStateEls.hide();
        form
          .find("[" + formStateName + "=loginError]")
          .stop(1)
          .fadeIn()
          .delay(3000)
          .fadeOut();
      });
  });

  $(".entryForm").submit(function (e) {
    e.preventDefault();
    var form = $(this);
    var action = serverUrl + form.attr("action");
    var formData =
      form.serialize() +
      "&" +
      loginUsnParam +
      "&" +
      $.param({ entryDateTime: moment().format("YYYY-MM-DD H:mm:ss") });

    $.ajax({
      url: action,
      method: "POST",
      processData: false,
      data: formData,
    })
      .done(function (data) {
        formStateEls.hide();
        form
          .find("[" + formStateName + "=entrySuccess]")
          .stop(1)
          .fadeIn()
          .delay(3000)
          .fadeOut();
      })
      .fail(function (data) {
        formStateEls.hide();
        form
          .find("[" + formStateName + "=entryError]")
          .stop(1)
          .fadeIn()
          .delay(3000)
          .fadeOut();
      });
  });
  $("#registrationForm").submit(function (e) {
    e.preventDefault();
    var form = $(this);
    var action = serverUrl + "/register";
    var formData = form.serialize();
    $.ajax({
      url: action,
      method: "POST",
      processData: false,
      data: formData,
    })
      .done(function (data) {
        formStateEls.hide();
        form
          .find("[" + formStateName + "=registrationSuccess]")
          .stop(1)
          .fadeIn()
          .delay(3000)
          .fadeOut();
      })
      .fail(function (data) {
        formStateEls.hide();
        form
          .find("[" + formStateName + "=registrationError]")
          .stop(1)
          .fadeIn()
          .delay(3000)
          .fadeOut();
      });
  });

  document.querySelector("body").addEventListener("submit", function (e) {
    if (e.target.id === "time-popover-form") {
      e.preventDefault();
      var form = $(e.target);
      new_topic_count_array = form.serializeArray();
      updateDailyTopicCounts(new_topic_count_array);
    }
  });

  async function loadInitialData() {
    await Promise.all([
      fillFilterCombos(),
      fillEntryCombos(),
      fillDailyTopicCountCombo(),
    ]);
  }

  function whenNotForethoughtCoach() {
    $("#coach-revision-section").hide("slow");
    $("#afterthought-strategies").hide();
    $("#forethought-strategy-section").show("slow");
    $("#afterthought-strategy").remove();
    formSelectors = [
      "#loginForm",
      "#forethoughtTopicEntry",
      ".forethoughtStrategyEntry",
    ];
    forethoughtConversationElement = null;
  }

  function whenForethoughtCoach(data) {
    whenNotForethoughtCoach();
    $("#coach-revision-section").show("slow");
    $("#afterthought-strategies").show();
    $("#forethought-strategy-section").hide("slow");
$(    `                  
    <div class="form-group">
      <label for="afterthought-strategy">Effort to apply strategy</label>
      <select required name="entryDifficulty" cf-questions="Strategy applied" id="afterthought-strategy"
        class="form-control">
        <option value="">unselected</option>
        <option value="1">survey</option>
        <option value="2">question</option>
        <option value="3">summarize</option>
        <option value="4">other</option>
      </select>
    </div>
`).prependTo("#statusEntryForm");
    formSelectors = ["#loginForm", "#forethoughtTopicEntry"];
    forethoughtConversationElement = window.cf.ConversationalForm.startTheConversation(
      forethoughtConversationJson(data)
    );
  }

  forethoughtCoachStateTransition(
    whenForethoughtCoach,
    whenNotForethoughtCoach
  );

  function initialState() {
    loadInitialData();
    whenNotForethoughtCoach();
    $("#daily-topic-count-alert").hide();
  }

  initialState();

  filterUsing(".filters");

  filterUsing("#usn", function (event) {
    if (event.target.value == "unselected" || event.target.value == "") {
      $("#table").bootstrapTable("showColumn", "Total Students (%)");
      $("#table").bootstrapTable("hideColumn", "Obtained Marks (%)");
    } else {
      $("#table").bootstrapTable("hideColumn", "Total Students (%)");
      $("#table").bootstrapTable("showColumn", "Obtained Marks (%)");
    }
  });

  function updateDailyTopicCounts(query_array) {
    var action = serverUrl + "/dailyTopicCount";
    var params = $.param(
      query_array.concat([
        {
          name: "currentDate",
          value: moment().format("YYYY-MM-DD"),
        },
      ])
    );
    $.ajax({
      url: action + "?" + params,
      method: "GET",
      processData: false,
    })
      .done(function (data) {
        $("#daily-topics-to-study").text(data["to_study"]);
        $("#daily-topics-studied").text(data["studied"]);
        $("#daily-topic-count-alert").hide();
        $("#daily-topic-count-alert").show("slow");
      })
      .fail(function (data) {
        alert("failure");
        $("#daily-topic-count-alert").hide("slow");
      });
  }
});
