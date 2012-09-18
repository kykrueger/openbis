/*
 * Copyright 2012 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.uitest.page;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Function;

import ch.systemsx.cisd.openbis.uitest.infra.PageProxy;
import ch.systemsx.cisd.openbis.uitest.infra.ScreenShotProxy;
import ch.systemsx.cisd.openbis.uitest.infra.ScreenShotter;
import ch.systemsx.cisd.openbis.uitest.infra.SeleniumTest;

public abstract class Page
{

    private PageProxy pageProxy;

    private ScreenShotter shotter;

    public void setPageProxy(PageProxy proxy)
    {
        this.pageProxy = proxy;
    }

    public void setScreenShotter(ScreenShotter shotter)
    {
        this.shotter = shotter;
    }

    public <T extends Page> T get(Class<T> clazz)
    {
        return this.pageProxy.get(clazz);
    }

    public WebElement findElement(WebElement element, String xpath)
    {
        return (WebElement) ScreenShotProxy.newInstance(element.findElement(By.xpath(xpath)),
                shotter);
    }

    public Collection<WebElement> findElements(WebElement element, String xpath)
    {
        SeleniumTest.driver.manage().timeouts().implicitlyWait(0, TimeUnit.MILLISECONDS);
        Collection<WebElement> elements = element.findElements(By.xpath(xpath));
        SeleniumTest.driver.manage().timeouts().implicitlyWait(SeleniumTest.IMPLICIT_WAIT,
                TimeUnit.MILLISECONDS);
        List<WebElement> wrapped = new ArrayList<WebElement>();
        for (WebElement e : elements)
        {
            wrapped.add((WebElement) ScreenShotProxy.newInstance(e, shotter));
        }
        return wrapped;
    }

    public void wait(final By by)
    {
        ExpectedCondition<?> condition = new ExpectedCondition<WebElement>()
            {
                @Override
                public WebElement apply(WebDriver d)
                {
                    return d.findElement(by);
                }
            };

        new WebDriverWait(SeleniumTest.driver, 10).until(condition);
    }

    public void waitForClickability(final WebElement element)
    {

        new WebDriverWait(SeleniumTest.driver, 10).until(new Function<WebDriver, Object>()
            {
                @Override
                public Object apply(WebDriver arg0)
                {
                    if (element.isDisplayed() && element.isEnabled())
                    {
                        return true;
                    } else
                    {
                        return null;
                    }
                }

            });
    }
}
