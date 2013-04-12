package aurora.plugin.source.gen.screen.model.asm;

import java.util.List;

import uncertain.composite.CompositeMap;
import aurora.database.service.DatabaseServiceFactory;
import aurora.plugin.source.gen.screen.model.AuroraComponent;
import aurora.plugin.source.gen.screen.model.Dataset;
import aurora.plugin.source.gen.screen.model.Form;
import aurora.plugin.source.gen.screen.model.Input;
import aurora.plugin.source.gen.screen.model.Label;

public class FormCreator extends AbstractModelCreator {

	public FormCreator(DatabaseServiceFactory svcFactory, CompositeMap context) {
		super(svcFactory, context);
	}

	@Override
	public void decorateComponent(AuroraComponent com, CompositeMap formPart)
			throws Exception {
		Form form = (Form) com;
		try {
			CompositeMap formMap = getFormMap(formPart.get("part_id"));
			form.setTitle(getFormTitle(formMap.getString("entity_name")));
			Dataset ds = form.getDataset();
			ds.setModel(getEntityModelPath(formMap.get("entity_id")));
			form.setCol(formMap.getInt("col", form.getCol()));
			CompositeMap itemsMap = getFormItems(formPart.get("part_id"));
			if (itemsMap != null) {
				@SuppressWarnings("unchecked")
				List<CompositeMap> itemList = itemsMap.getChildsNotNull();
				for (CompositeMap m : itemList) {
					String editor = m.getString("editor");
					if (isViewPage() || "label".equals(editor)) {
						Label l = new Label();
						l.setName(m.getString("name"));
						l.setPrompt(m.getString("prompt"));
						form.addChild(l);
					} else {
						Input input = createInput(editor);
						if (input != null) {
							input.setName(m.getString("name"));
							input.setPrompt(m.getString("prompt"));
							form.addChild(input);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String getFormTitle(String entity_name) {
		return (isViewPage()?"查看":"编辑")+" - "+entity_name;
	}

	@Override
	public AuroraComponent create(CompositeMap formPart) throws Exception {
		Form form = new Form();
		decorateComponent(form, formPart);
		return form;
	}

	private CompositeMap getFormMap(Object formId) throws Exception {
		CompositeMap para = new CompositeMap();
		para.put("custom_form_id", formId);
		return PageGenerator.queryFirst(getDatabaseServiceFactory(),
				getContext(), "page.custom_form_for_query", para);

	}

	private CompositeMap getFormItems(Object formId) throws Exception {
		CompositeMap para = new CompositeMap();
		para.put("custom_form_id", formId);
		return PageGenerator.query(getDatabaseServiceFactory(), getContext(),
				"page.custom_form_item_for_query", para);
	}

}