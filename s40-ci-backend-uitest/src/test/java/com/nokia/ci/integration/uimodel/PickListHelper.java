package com.nokia.ci.integration.uimodel;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: djacko
 * Date: 1/28/13
 * Time: 9:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class PickListHelper {

    private final static int ADD_BUTTON = 0;
    private final static int ADD_ALL_BUTTON = 1;
    private final static int REMOVE_BUTTON = 2;
    private final static int REMOVE_ALL_BUTTON = 3;

    private final static int SOURCE_COLUMN = 0;
    private final static int BUTTON_COLUMN = 1;
    private final static int TARGET_COLUMN = 2;


    public static List<String> getSourceOptionsAsString(WebElement pickList) {
        List<String> ret = new ArrayList<String>();
        List<WebElement> pickListColumns = pickList.findElements(By.tagName("td"));
        WebElement sourceColumn = pickListColumns.get(SOURCE_COLUMN);
        List<WebElement> options = sourceColumn.findElements(By.tagName("li"));
        for (WebElement option : options) {
            String optionAsString = option.getText();
            ret.add(optionAsString);
        }
        return ret;
    }

    public static List<String> getTargetOptionsAsString(WebElement pickList) {
        List<String> ret = new ArrayList<String>();
        List<WebElement> pickListColumns = pickList.findElements(By.tagName("td"));
        WebElement targetColumn = pickListColumns.get(TARGET_COLUMN);
        List<WebElement> options = targetColumn.findElements(By.tagName("li"));
        for (WebElement option : options) {
            String optionAsString = option.getText();
            ret.add(optionAsString);
        }
        return ret;
    }

    public static WebElement findSourceByLabel(WebElement pickList, String optionLabel) {
        List<WebElement> pickListColumns = pickList.findElements(By.tagName("td"));
        WebElement sourceColumn = pickListColumns.get(SOURCE_COLUMN);
        List<WebElement> options = sourceColumn.findElements(By.tagName("li"));
        for (WebElement option : options) {
            String optionAsString = option.getText();
            if (optionAsString.contains(optionLabel)) {
                return option;
            }

        }
        return null;
    }

    public static WebElement findTargetByLabel(WebElement pickList, String optionLabel) {
        List<WebElement> pickListColumns = pickList.findElements(By.tagName("td"));
        WebElement sourceColumn = pickListColumns.get(TARGET_COLUMN);
        List<WebElement> options = sourceColumn.findElements(By.tagName("li"));
        for (WebElement option : options) {
            String optionAsString = option.getText();
            if (optionAsString.contains(optionLabel)) {
                return option;
            }

        }
        return null;
    }

    public static boolean containSourceLabel(WebElement pickList, String optionLabel) {
        List<WebElement> pickListColumns = pickList.findElements(By.tagName("td"));
        WebElement sourceColumn = pickListColumns.get(SOURCE_COLUMN);
        List<WebElement> options = sourceColumn.findElements(By.tagName("li"));
        for (WebElement option : options) {
            String optionAsString = option.getText();
            if (optionAsString.contains(optionLabel)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containTargetLabel(WebElement pickList, String optionLabel) {
        List<WebElement> pickListColumns = pickList.findElements(By.tagName("td"));
        WebElement sourceColumn = pickListColumns.get(TARGET_COLUMN);
        List<WebElement> options = sourceColumn.findElements(By.tagName("li"));
        for (WebElement option : options) {
            String optionAsString = option.getText();
            if (optionAsString.contains(optionLabel)) {
                return true;
            }
        }
        return false;
    }

    public static WebElement getAddBtn(WebElement pickList) {
        List<WebElement> pickListColumns = pickList.findElements(By.tagName("td"));
        WebElement buttonsColumn = pickListColumns.get(BUTTON_COLUMN);
        List<WebElement> buttons = buttonsColumn.findElements(By.tagName("button"));
        return buttons.get(ADD_BUTTON);
    }

    public static WebElement getAddAllBtn(WebElement pickList) {
        List<WebElement> pickListColumns = pickList.findElements(By.tagName("td"));
        WebElement buttonsColumn = pickListColumns.get(BUTTON_COLUMN);
        List<WebElement> buttons = buttonsColumn.findElements(By.tagName("button"));
        return buttons.get(ADD_ALL_BUTTON);
    }

    public static WebElement getRemoveBtn(WebElement pickList) {
        List<WebElement> pickListColumns = pickList.findElements(By.tagName("td"));
        WebElement buttonsColumn = pickListColumns.get(BUTTON_COLUMN);
        List<WebElement> buttons = buttonsColumn.findElements(By.tagName("button"));
        return buttons.get(REMOVE_BUTTON);
    }

    public static WebElement getRemoveAllBtn(WebElement pickList) {
        List<WebElement> pickListColumns = pickList.findElements(By.tagName("td"));
        WebElement buttonsColumn = pickListColumns.get(BUTTON_COLUMN);
        List<WebElement> buttons = buttonsColumn.findElements(By.tagName("button"));
        return buttons.get(REMOVE_ALL_BUTTON);
    }

}/*


	private List<WebElement> cities;

	private WebElement addButton;

	private WebElement addAllButton;

	private WebElement removeButton;

	private WebElement removeAllButton;

	@Before
	public void before() {
		driver.get(toShowcaseUrl("picklist.jsf"));

		List<WebElement> pickListColumns = findElementById("form:pickList").findElements(By.tagName("td"));
		WebElement sourceCitiesColumn = pickListColumns.get(0);
		WebElement buttonsColumn = pickListColumns.get(1);
		cities = sourceCitiesColumn.findElements(By.tagName("li"));
		List<WebElement> buttons = buttonsColumn.findElements(By.tagName("button"));

		addButton = buttons.get(0);
		addAllButton = buttons.get(1);
		removeButton = buttons.get(2);
		removeAllButton = buttons.get(3);
	}

	@Test
	public void shouldAddTwoItems() {

		List<String> selectedCities = new ArrayList<String>();

		select(selectedCities, cities.get(0));
		select(selectedCities, cities.get(1));

		submitForm();

		verifyRemovedFromSourceCities(selectedCities);
		verifyAddedToTargetCities(selectedCities);

	}

	@Test
	public void shouldAddAllItems() {

		List<String> selectedCities = new ArrayList<String>();

		clickAndWait(addAllButton);

		submitForm();

		WebElement sourceCities = getCities(true);
		assertThat(sourceCities.getText(), equalTo(""));
		verifyAddedToTargetCities(selectedCities);

	}

	@Test
	public void shouldAddAndRemoveItems() {

		List<String> selectedCities = new ArrayList<String>();

		select(selectedCities, cities.get(0));
		select(selectedCities, cities.get(1));
		removeFirstClickedItem(selectedCities);

		submitForm();

		verifyRemovedFromSourceCities(selectedCities);
		verifyAddedToTargetCities(selectedCities);
	}

	@Test
	public void shouldAddAndRemoveAllItems() {
		List<String> selectedCities = new ArrayList<String>();
		for (WebElement city : cities) {
			select(selectedCities, city);
		}

		clickAndWait(removeAllButton);

		submitForm();

		WebElement sourceCities = getCities(true);
		for (String selectedCity : selectedCities) {
			assertThat(sourceCities.getText(), containsString(selectedCity));
		}

		WebElement targetCities = getCities(false);
		assertThat(targetCities.getText(), equalTo(""));
	}

	private void clickAndWait(WebElement element) {
		element.click();
		waitForOneSecond();
	}

	private void submitForm() {
		clickToElementById("form:citySubmit");
		waitUntilAjaxRequestCompletes();
	}

	private void verifyRemovedFromSourceCities(List<String> cities) {
		WebElement sourceCities = getCities(true);

		for (String selectedCity : cities) {
			assertThat(sourceCities.getText(), not(containsString(selectedCity)));
		}

	}

	private void verifyAddedToTargetCities(List<String> cities) {
		WebElement targetCities = getCities(false);

		for (String selectedCity : cities) {
			assertThat(targetCities.getText(), containsString(selectedCity));
		}
	}

	private WebElement getCities(boolean isSource) {
		List<WebElement> selectedCityRows = findElementById("form:displayCities").findElements(By.tagName("tr"));
		return selectedCityRows.get(isSource ? 0 : 1).findElements(By.tagName("td")).get(1);
	}

	private void select(List<String> selectedCities, WebElement city) {
		selectedCities.add(city.getText());
		city.click();
		clickAndWait(addButton);
	}

	private void removeFirstClickedItem(List<String> cities) {
		List<WebElement> targetCities = findElementById("form:pickList").findElements(By.tagName("td")).get(2).findElements(By.tagName("li"));
		WebElement city = targetCities.get(0);
		cities.remove(city.getText());
		city.click();
		clickAndWait(removeButton);
	}

}

*/
