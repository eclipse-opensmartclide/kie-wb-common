/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.bpmn.client.forms.fields.assignmentsEditor;

import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.google.gwt.dom.client.Style;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.ListGroup;
import org.gwtbootstrap3.client.ui.ListGroupItem;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.ColumnSize;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.kie.workbench.common.stunner.bpmn.client.forms.fields.i18n.StunnerFormsClientFieldsConstants;
import org.kie.workbench.common.stunner.bpmn.client.forms.fields.model.AssignmentRow;
import org.kie.workbench.common.stunner.bpmn.client.forms.fields.model.Variable;
import org.kie.workbench.common.stunner.bpmn.client.forms.util.ListBoxValues;
import org.uberfire.ext.widgets.common.client.common.popups.BaseModal;

@Dependent
public class ActivityDataIOEditorViewImpl extends BaseModal implements ActivityDataIOEditorView,
                                                                       NotifyAddDataType {

    protected Presenter presenter;

    @Inject
    protected ActivityDataIOEditorWidget inputAssignmentsWidget;

    @Inject
    protected ActivityDataIOEditorWidget outputAssignmentsWidget;

    protected Button btnOk;

    private Button btnCancel;

    private Container container = new Container();

    private Row row = new Row();

    private Column column = new Column(ColumnSize.MD_12);

    public static final int EXPRESSION_MAX_DISPLAY_LENGTH = 65;

    public Row SmartCLIDERowSearch;

    public String taskDocumentation;

    public String taskTitle;

    private String urlTheia = "SMARTCLIDE_THEIA_URL";
    private String urlServiceDiscovery = "SMARTCLIDE_SERVICE_DISCOVERY_URL";

    public ActivityDataIOEditorViewImpl() {
        super();
    }

    public void init(final Presenter presenter) {
        this.presenter = presenter;
        container.setFluid(true);
        container.add(row);
        row.add(column);
        setTitle(StunnerFormsClientFieldsConstants.CONSTANTS.Data_IO());
        inputAssignmentsWidget.setVariableType(Variable.VariableType.INPUT);
        inputAssignmentsWidget.setNotifier(this);
        inputAssignmentsWidget.setAllowDuplicateNames(false,
                                                      StunnerFormsClientFieldsConstants.CONSTANTS.A_Data_Input_with_this_name_already_exists());
        column.add(inputAssignmentsWidget.getWidget());
        outputAssignmentsWidget.setVariableType(Variable.VariableType.OUTPUT);
        outputAssignmentsWidget.setNotifier(this);
        outputAssignmentsWidget.setAllowDuplicateNames(true,
                                                       "");
        column.add(outputAssignmentsWidget.getWidget());


        /**SmartCLIDE addition**/
        /**Service Discovery**/
        SmartCLIDERowSearch = new Row();
        SmartCLIDERowSearch.getElement().getStyle().setMarginTop(10, Style.Unit.PX);
        SmartCLIDERowSearch.getElement().getStyle().setDisplay(Style.Display.NONE);
        final Column SmartCLIDEColumnSearch = new Column(ColumnSize.MD_12);
        SmartCLIDERowSearch.add(SmartCLIDEColumnSearch);

        //Row for Service Discovery label
        Row SmartCLIDERowInner1 = new Row();
        Column SmartCLIDEColumnInner1 = new Column(ColumnSize.MD_12);
        SmartCLIDERowInner1.add(SmartCLIDEColumnInner1);
        Label label = new Label("SmartCLIDE service discovery");
        label.setPull(Pull.LEFT);
        label.getElement().setAttribute("style", "font-size: 16px; margin-top: 20px;" +
                " margin-bottom: 10px; font-weight: 600; line-height: 1.1; color: inherit;" +
                " display: block; background: transparent; padding: 0px;");
        SmartCLIDEColumnInner1.add(label);

        //Row for button and list
        Row SmartCLIDERowInner2 = new Row();
        Column SmartCLIDEColumnInner2 = new Column(ColumnSize.MD_12);
        SmartCLIDERowInner2.add(SmartCLIDEColumnInner2);
        Button btnSearch = new Button("Search");
        btnSearch.getElement().getStyle().setBackgroundImage("linear-gradient(to bottom,rgb(53 181 191) 0,rgb(67 103 162) 100%)");
        btnSearch.getElement().getStyle().setColor("#ffffff");
        btnSearch.setPull(Pull.LEFT);
        btnSearch.addClickHandler(clickEvent -> {
            //Create List with ListItems
            ListGroup listGroup= new ListGroup();
            listGroup.getElement().setAttribute("style","margin-bottom: 0px;");

            //Call Service Discovery API
            try {
                RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, urlServiceDiscovery);
                builder.setHeader("Content-Type", "application/json");
                String jsonInputString3 = "\"{\\\"full_name\\\":{\\\"0\\\":\\\"" + this.taskTitle +
                            "\\\"}, \\\"description\\\": {\\\"0\\\":\\\"" + this.taskDocumentation + "\\\"}}\"";
                Request response = builder.sendRequest(jsonInputString3, new RequestCallback() {
                    public void onError(Request request, Throwable exception) { }
                    public void onResponseReceived(Request request, Response response) {
                        String sss= response.getText().substring(1, response.getText().length()-2);
                        sss = sss.replace("\\\"{", "{");
                        sss = sss.replace("}\\\"", "}");
                        sss = sss.replace("\\\\\\\"", "\"");
                        JSONArray jsonArray = (JSONArray) JSONParser.parse(sss);

                        //For each service create a List Item
                        for(int i=0; i<jsonArray.size(); i++) {
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                            Double score= Double.parseDouble(jsonObject.get("_score").toString());
                            JSONObject jsonObject2= (JSONObject) jsonObject.get("_source");
                            String fullName = jsonObject2.get("full_name").toString().substring(1, jsonObject2.get("full_name").toString().length()-1);
                            String description = jsonObject2.get("description").toString().substring(1, jsonObject2.get("description").toString().length()-1);
                            String link = jsonObject2.get("link").toString().substring(1, jsonObject2.get("link").toString().length()-1);
                            String source = jsonObject2.get("source").toString().substring(1, jsonObject2.get("source").toString().length()-1);

                            ListGroupItem listGroupItem1= new ListGroupItem();
                            Div divOuter = new Div();
                            divOuter.getElement().setAttribute("style","display: flex; justify-content: space-between;");
                            Div divInner1 = new Div();
                            Span spanName = new Span();
                            spanName.setText(fullName);
                            spanName.getElement().setAttribute("style","font-size: 13px; font-weight: bold; margin-right: 5px;");
                            Anchor anchor = new Anchor("("+ source +")",link);
                            anchor.getElement().setAttribute("target","_blank");
                            Span spanScore = new Span();
                            spanScore.setText("score: " + score);
                            spanScore.getElement().getStyle().setFontStyle(Style.FontStyle.ITALIC);
                            spanScore.getElement().getStyle().setDisplay(Style.Display.BLOCK);
                            Span spanDescriptionOuter = new Span();
                            Span spanDescription = new Span();
                            if(description.length() > 50){
                                //if there are many chars in description add button for show more/less
                                spanDescription.setText(description.substring(0,50));
                                Button buttonMoreDescription = new Button("Show More");
                                buttonMoreDescription.getElement().setAttribute("style", "background: none;" +
                                        " color: inherit; border: none; padding: 0; font: inherit;" +
                                        " cursor: pointer; outline: inherit; font-weight: bold;" +
                                        " padding-bottom: 2px; color: #0088ce; box-shadow: none; margin-left: 5px;");
                                buttonMoreDescription.addClickHandler(clickEvent1 -> {
                                    if(buttonMoreDescription.getText().equals("Show More")) {
                                        spanDescription.setText(description);
                                        buttonMoreDescription.setText("Show Less");
                                    }
                                    else{
                                        spanDescription.setText(description.substring(0,50));
                                        buttonMoreDescription.setText("Show More");
                                    }
                                });
                                spanDescriptionOuter.add(spanDescription);
                                spanDescriptionOuter.add(buttonMoreDescription);
                            }
                            else{
                                spanDescription.setText(description);
                                spanDescriptionOuter.add(spanDescription);
                            }
                            spanDescriptionOuter.getElement().getStyle().setDisplay(Style.Display.BLOCK);
                            divInner1.add(spanName);
                            divInner1.add(anchor);
                            divInner1.add(spanScore);
                            divInner1.add(spanDescriptionOuter);
                            divOuter.add(divInner1);
                            Div divInner2 = new Div();
                            divInner2.getElement().setAttribute("style","display: flex; flex-direction: column; justify-content: space-around;");
                            Button btnDeploy = new Button("Deploy");
                            //btnDeploy.addClickHandler(clickEvent1 -> textArea.setText("Deploy "+spanName.getText()));
                            Button btnUse = new Button("Use");
                            btnUse.addClickHandler(clickEvent1 -> {
                                //add assignment to variable
                                for(int k=0; k<inputAssignmentsWidget.view.getAssignmentsCount(); k++){
                                    if(inputAssignmentsWidget.view.getAssignmentRows().get(k).getName().equals("Method")){
                                        inputAssignmentsWidget.view.getAssignmentWidget(k).setExpression("GET");
                                        inputAssignmentsWidget.view.getAssignmentWidget(k).setProcessVarComboBoxText("GET");
                                    }
                                }
                            });
                            divInner2.add(btnDeploy);
                            divInner2.add(btnUse);
                            divOuter.add(divInner2);
                            listGroupItem1.add(divOuter);
                            listGroup.add(listGroupItem1);
                        }}
                 });
            } catch (RequestException e) {
                e.printStackTrace();
            }

            //Row for List
            Row SmartCLIDERowInner3 = new Row();
            Column SmartCLIDEColumnInner3 = new Column(ColumnSize.MD_12);
            SmartCLIDERowInner3.add(SmartCLIDEColumnInner3);
            SmartCLIDEColumnInner3.add(listGroup);
            SmartCLIDEColumnSearch.add(SmartCLIDERowInner3);
        });
        SmartCLIDEColumnInner2.add(btnSearch);

        SmartCLIDEColumnSearch.add(SmartCLIDERowInner1);
        SmartCLIDEColumnSearch.add(SmartCLIDERowInner2);
        container.add(SmartCLIDERowSearch);
        /**Service Discovery**/

        /**Service Creation**/
        Row SmartCLIDERowCreation = new Row();
        SmartCLIDERowCreation.getElement().getStyle().setMarginTop(10, Style.Unit.PX);
        Column SmartCLIDEColumnCreation = new Column(ColumnSize.MD_12);
        SmartCLIDERowCreation.add(SmartCLIDEColumnCreation);
        //Label
        Row SmartCLIDERowInnerCreation1 = new Row();
        Column SmartCLIDEColumnInnerCreation1 = new Column(ColumnSize.MD_12);
        SmartCLIDERowInnerCreation1.add(SmartCLIDEColumnInnerCreation1);
        Label labelC = new Label("SmartCLIDE service creation");
        labelC.setPull(Pull.LEFT);
        labelC.getElement().setAttribute("style", "font-size: 16px; margin-top: 20px;" +
                " margin-bottom: 10px; font-weight: 600; line-height: 1.1; color: inherit;" +
                " display: block; background: transparent; padding: 0px;");
        SmartCLIDEColumnInnerCreation1.add(labelC);
        //Button Create
        Row SmartCLIDERowInnerCreation2 = new Row();
        Column SmartCLIDEColumnInnerCreation2 = new Column(ColumnSize.MD_12);
        SmartCLIDERowInnerCreation2.add(SmartCLIDEColumnInnerCreation2);
        Div divServiceCreation = new Div();
        divServiceCreation.getElement().setAttribute("style","display: flex; justify-content: flex-start;");
        Button btnCreate = new Button("Create");
        btnCreate.getElement().getStyle().setBackgroundImage("linear-gradient(to bottom,rgb(53 181 191) 0,rgb(67 103 162) 100%)");
        btnCreate.getElement().getStyle().setColor("#ffffff");
        btnCreate.addClickHandler(clickEvent1 -> {
            //ToDo
            //start Theia
            Window.open(urlTheia,"_blank","");
        });
        divServiceCreation.add(btnCreate);
        Span spanCreteServiceText = new Span();
        spanCreteServiceText.setText("If you can't find a suitable service you can create your own!");
        spanCreteServiceText.getElement().setAttribute("style","font-weight: 400; font-style: italic; margin-left: 5px; margin-top: 2px;");
        divServiceCreation.add(spanCreteServiceText);
        SmartCLIDEColumnInnerCreation2.add(divServiceCreation);

        SmartCLIDEColumnCreation.add(SmartCLIDERowInnerCreation1);
        SmartCLIDEColumnCreation.add(SmartCLIDERowInnerCreation2);
        container.add(SmartCLIDERowCreation);
        /**Service Creation**/
        /**SmartCLIDE addition**/


        final Row btnRow = new Row();
        btnRow.getElement().getStyle().setMarginTop(10,
                                                    Style.Unit.PX);
        final Column btnColumn = new Column(ColumnSize.MD_12);
        btnRow.add(btnColumn);
        btnOk = new Button(StunnerFormsClientFieldsConstants.CONSTANTS.Ok());
        btnOk.setType(ButtonType.PRIMARY);
        btnOk.setPull(Pull.RIGHT);
        btnOk.addClickHandler(clickEvent -> presenter.handleOkClick());
        btnColumn.add(btnOk);
        btnCancel = new Button(StunnerFormsClientFieldsConstants.CONSTANTS.Cancel());
        btnCancel.setPull(Pull.RIGHT);
        btnCancel.addClickHandler(event -> presenter.handleCancelClick());
        btnColumn.add(btnCancel);
        container.add(btnRow);
        setWidth("1200px");
        setBody(container);
    }

    @Override
    public void onHide(final Event e) {
    }

    @Override
    public void setCustomViewTitle(final String name, final String documentation, final String taskCustomName) {
        setTitle(name + " " + StunnerFormsClientFieldsConstants.CONSTANTS.Data_IO());

        /**SmartCLIDE addition**/
        this.taskTitle = name;
        //show only for Rest tasks and get description
        if(taskCustomName.equals("Rest")) {
            SmartCLIDERowSearch.getElement().getStyle().setDisplay(Style.Display.BLOCK);
            this.taskDocumentation = documentation;
        }
        /**SmartCLIDE addition**/
    }

    @Override
    public void setDefaultViewTitle() {
        setTitle(StunnerFormsClientFieldsConstants.CONSTANTS.Data_IO());
    }

    @Override
    public void setInputAssignmentRows(final List<AssignmentRow> inputAssignmentRows) {
        inputAssignmentsWidget.setData(inputAssignmentRows);
    }

    @Override
    public void setOutputAssignmentRows(final List<AssignmentRow> outputAssignmentRows) {
        outputAssignmentsWidget.setData(outputAssignmentRows);
    }

    @Override
    public void setInputAssignmentsVisibility(final boolean visible) {
        inputAssignmentsWidget.setIsVisible(visible);
    }

    @Override
    public void setOutputAssignmentsVisibility(final boolean visible) {
        outputAssignmentsWidget.setIsVisible(visible);
    }

    @Override
    public void setIsInputAssignmentSingleVar(final boolean single) {
        inputAssignmentsWidget.setIsSingleVar(single);
    }

    @Override
    public void setIsOutputAssignmentSingleVar(final boolean single) {
        outputAssignmentsWidget.setIsSingleVar(single);
    }

    @Override
    public void hideView() {
        super.hide();
    }

    @Override
    public void showView() {
        super.show();
    }

    @Override
    public List<AssignmentRow> getInputAssignmentData() {
        return inputAssignmentsWidget.getData();
    }

    @Override
    public List<AssignmentRow> getOutputAssignmentData() {
        return outputAssignmentsWidget.getData();
    }

    @Override
    public void setPossibleInputAssignmentsDataTypes(final List<String> dataTypeDisplayNames) {
        ListBoxValues dataTypeListBoxValues = new ListBoxValues(AssignmentListItemWidgetView.CUSTOM_PROMPT,
                                                                StunnerFormsClientFieldsConstants.CONSTANTS.Edit() + " ",
                                                                presenter.dataTypesTester());
        dataTypeListBoxValues.addValues(dataTypeDisplayNames);
        inputAssignmentsWidget.setDataTypes(dataTypeListBoxValues);
    }

    @Override
    public void setPossibleOutputAssignmentsDataTypes(final List<String> dataTypeDisplayNames) {
        ListBoxValues dataTypeListBoxValues = new ListBoxValues(AssignmentListItemWidgetView.CUSTOM_PROMPT,
                                                                StunnerFormsClientFieldsConstants.CONSTANTS.Edit() + " ",
                                                                presenter.dataTypesTester(),
                                                                EXPRESSION_MAX_DISPLAY_LENGTH);
        dataTypeListBoxValues.addValues(dataTypeDisplayNames);
        outputAssignmentsWidget.setDataTypes(dataTypeListBoxValues);
    }

    @Override
    public void setInputAssignmentsProcessVariables(final List<String> processVariables) {
        ListBoxValues processVarListBoxValues = new ListBoxValues(AssignmentListItemWidgetView.EXPRESSION_PROMPT,
                                                                  StunnerFormsClientFieldsConstants.CONSTANTS.Edit() + " ",
                                                                  presenter.processVarTester(),
                                                                  EXPRESSION_MAX_DISPLAY_LENGTH);
        processVarListBoxValues.addValues(processVariables);
        inputAssignmentsWidget.setProcessVariables(processVarListBoxValues);
    }

    @Override
    public void setOutputAssignmentsProcessVariables(final List<String> processVariables) {
        ListBoxValues processVarListBoxValues = new ListBoxValues(AssignmentListItemWidgetView.EXPRESSION_PROMPT,
                                                                  StunnerFormsClientFieldsConstants.CONSTANTS.Edit() + " ",
                                                                  presenter.processVarTester());
        processVarListBoxValues.addValues(processVariables);
        outputAssignmentsWidget.setProcessVariables(processVarListBoxValues);
    }

    @Override
    public void setInputAssignmentsDisallowedNames(final Set<String> names) {
        inputAssignmentsWidget.setDisallowedNames(names,
                                                  StunnerFormsClientFieldsConstants.CONSTANTS.This_input_should_be_entered_as_a_property_for_the_task());
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        btnOk.setEnabled(!readOnly);
        inputAssignmentsWidget.setReadOnly(readOnly);
        outputAssignmentsWidget.setReadOnly(readOnly);
    }

    @Override
    public void addDataType(String dataType, String oldType) {
        presenter.addDataType(dataType, oldType);
    }

    @Override
    public void notifyAdd(String dataType, String oldType, final ListBoxValues dataTypeListBoxValues) {
        presenter.addDataType(dataType, oldType);
    }
}
