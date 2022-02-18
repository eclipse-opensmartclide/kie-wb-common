/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.bpmn.client.documentation;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.gwt.dom.client.Document;
import elemental2.dom.HTMLElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.workbench.common.stunner.bpmn.definition.BPMNDiagram;
import org.kie.workbench.common.stunner.bpmn.documentation.BPMNDocumentationService;
import org.kie.workbench.common.stunner.bpmn.qualifiers.BPMN;
import org.kie.workbench.common.stunner.core.client.i18n.ClientTranslationService;
import org.kie.workbench.common.stunner.core.client.util.PrintHelper;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.documentation.DefaultDiagramDocumentationView;
import org.kie.workbench.common.stunner.core.documentation.model.DocumentationOutput;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.definition.Definition;
import org.kie.workbench.common.stunner.core.i18n.CoreTranslationMessages;
import org.kie.workbench.common.stunner.core.util.DefinitionUtils;
import org.kie.workbench.common.stunner.forms.client.event.FormFieldChanged;
import org.uberfire.client.views.pfly.icon.PatternFlyIconType;
import org.uberfire.client.views.pfly.widgets.Button;

@BPMN
@Dependent
@Templated
public class BPMNDocumentationView extends DefaultDiagramDocumentationView {

    private BPMNDocumentationService documentationService;

    @Inject
    @Named("documentationDiv")
    @DataField
    private HTMLElement documentationDiv;

    @Inject
    @DataField
    private Button printButton;

    @Inject
    @DataField
    private Button tdButton;

    @Inject
    @Named("numberOfTasks")
    @DataField
    private HTMLElement numberOfTasks;

    @Inject
    @Named("noUserTask")
    @DataField
    private HTMLElement noUserTask;

    @Inject
    @Named("differentDataTypes")
    @DataField
    private HTMLElement differentDataTypes;

    @Inject
    @Named("lackDescription")
    @DataField
    private HTMLElement lackDescription;

    @Inject
    @Named("numberOfCustomTasks")
    @DataField
    private HTMLElement numberOfCustomTasks;

    @Inject
    private DefinitionUtils definitionUtils;

    private final ClientTranslationService clientTranslationService;

    private final PrintHelper printHelper;

    private Supplier<Boolean> isSelected;

    protected BPMNDocumentationView(final BPMNDocumentationService documentationService,
                                    final ClientTranslationService clientTranslationService,
                                    final PrintHelper printHelper,
                                    final HTMLElement documentationDiv,
                                    final Button printButton) {
        this.documentationService = documentationService;
        this.clientTranslationService = clientTranslationService;
        this.printHelper = printHelper;
        this.documentationDiv = documentationDiv;
        this.printButton = printButton;
    }

    @Inject
    public BPMNDocumentationView(final BPMNDocumentationService documentationService,
                                 final ClientTranslationService clientTranslationService,
                                 final PrintHelper printHelper) {
        this.documentationService = documentationService;
        this.clientTranslationService = clientTranslationService;
        this.printHelper = printHelper;
    }

    @Override
    public BPMNDocumentationView setIsSelected(final Supplier<Boolean> isSelected) {
        this.isSelected = isSelected;
        return this;
    }

    @Override
    public BPMNDocumentationView initialize(Diagram diagram) {
        super.initialize(diagram);

        printButton.setText(clientTranslationService.getValue(CoreTranslationMessages.PRINT));
        printButton.addIcon(PatternFlyIconType.PRINT.getCssName(), "pull-right");
        printButton.setClickHandler(() -> print());

        /** TD of workflow **/
        tdButton.setText("Calculate TD");
        tdButton.setClickHandler(() -> {
            Document.get().getElementById("tdContent").setAttribute("style","display: block;");

            String names = "";
            //String categ= "";
            Iterator<Node> iterator = diagram.getGraph().nodes().iterator();
            while ((iterator.hasNext())) {
                Node n = iterator.next();
                if (n.getContent() instanceof Definition && n.getContent()!=null){
                    Definition d = (Definition) n.getContent();
                    if (!(d.getDefinition() instanceof BPMNDiagram) && d.getDefinition()!=null){
                        names+=definitionUtils.getName(d.getDefinition()) +" ";
                        //categ+= definitionHelper.getDefinitionCategory(d.getDefinition())+ " ";
                    }
                }
            }
            numberOfTasks.innerHTML = ""+names;
            //noUserTask.innerHTML= ""+categ;
        });

        numberOfTasks.innerHTML = "10";
        noUserTask.innerHTML = "Yes";
        differentDataTypes.innerHTML = "10";
        lackDescription.innerHTML = "2";
        numberOfCustomTasks.innerHTML = "5";
        /** TD of workflow **/

        return refresh();
    }

    void print() {
        printHelper.print(documentationDiv);
    }

    @Override
    public BPMNDocumentationView refresh() {
        documentationDiv.innerHTML = getDocumentationHTML();
        return this;
    }

    protected void onFormFieldChanged(@Observes FormFieldChanged formFieldChanged) {
        Optional.ofNullable(isSelected)
                .map(Supplier::get)
                .filter(Boolean.TRUE::equals)
                .map(focus -> getDiagram()
                        .map(d -> d.getGraph().getNode(formFieldChanged.getUuid()))
                ).ifPresent(focus -> refresh());
    }

    private String getDocumentationHTML() {
        return getDiagram()
                .map(documentationService::generate)
                .map(DocumentationOutput::getValue)
                .orElse("");
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}