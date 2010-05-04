package org.purc.purcforms.client.view;

import org.purc.purcforms.client.locale.LocaleText;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;


/**
 * 
 * @author daniel
 *
 */
public class SaveFileDialog extends DialogBox{

	private FormPanel form = new FormPanel();
	private String actionUrl;
	private TextBox txtName;
	private TextArea txtArea;

	public SaveFileDialog(String url, String data, String fileName){
		initWidgets(url,data,fileName);
	}

	public void initWidgets(String url, String data, String fileName){
		actionUrl = url;
		form.setAction(actionUrl);
		form.setEncoding(FormPanel.ENCODING_MULTIPART);
		form.setMethod(FormPanel.METHOD_POST);

		VerticalPanel verticalPanel = new VerticalPanel();
		verticalPanel.setSpacing(20);
		form.add(verticalPanel);

		txtArea = new TextArea();
		txtArea.setText(data);
		txtArea.setName("filecontents");
		txtArea.setVisible(false);

		txtName = new TextBox();
		txtName.setText(fileName);
		txtName.setName("filename");
		txtName.setWidth("200px");
		verticalPanel.add(txtName);
		verticalPanel.add(txtArea);

		HorizontalPanel horizontalPanel = new HorizontalPanel();
		horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		horizontalPanel.add(new Button(LocaleText.get("save"), new ClickHandler(){
			public void onClick(ClickEvent event){
				String fileName = txtName.getText();
				if(fileName != null && fileName.trim().length() > 0){
					String action = actionUrl;
					if(action.contains("?"))
						action += "&";
					else
						action += "?";
					action += "filename="+fileName;
					
					form.setAction(action);
					((VerticalPanel)txtName.getParent()).add(txtArea);
					form.submit();
				}
			}
		}));

		horizontalPanel.add(new Button(LocaleText.get("cancel"), new ClickHandler(){
			public void onClick(ClickEvent event){
				hide();
			}
		}));

		verticalPanel.add(horizontalPanel);

		setWidget(form);

		form.addSubmitCompleteHandler(new SubmitCompleteHandler(){
			public void onSubmitComplete(FormPanel.SubmitCompleteEvent event){
				hide();
				Window.Location.replace(form.getAction());
			}
		});

		setText(LocaleText.get("saveFileAs"));
	}
}