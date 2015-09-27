package com.jetbrains.lang.dart.ide;

import com.intellij.conversion.*;
import com.intellij.javascript.debugger.execution.JavascriptDebugConfigurationType;
import com.intellij.openapi.util.text.StringUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

public class DartRunConfigurationConverterProvider extends ConverterProvider {
  protected DartRunConfigurationConverterProvider() {
    super("dart-run-configurations-converter");
  }

  @NotNull
  @Override
  public String getConversionDescription() {
    return "Dart run configurations will be converted into JavaScript run configurations";
  }

  @NotNull
  @Override
  public ProjectConverter createConverter(@NotNull ConversionContext context) {
    return new ProjectConverter() {
      @Override
      public ConversionProcessor<RunManagerSettings> createRunConfigurationsConverter() {
        return new ConversionProcessor<RunManagerSettings>() {
          @Override
          public boolean isConversionNeeded(RunManagerSettings settings) {
            for (Element element : settings.getRunConfigurations()) {
              if (DartRunConfigurationConverterProvider.isConversionNeeded(element)) {
                return true;
              }
            }
            return false;
          }

          @Override
          public void process(RunManagerSettings settings) {
            for (Element element : settings.getRunConfigurations()) {
              if (DartRunConfigurationConverterProvider.isConversionNeeded(element)) {
                converter(element);
              }
            }
          }
        };
      }
    };
  }

  public static void converter(Element element) {
    if (!isConversionNeeded(element)) return;
    if ("DartConfigurationType".equalsIgnoreCase(element.getAttributeValue("type"))) {
      convertLocal(element);
    }
    else if ("DartDebugSession".equalsIgnoreCase(element.getAttributeValue("type"))) {
      convertRemote(element);
    }
  }

  private static void convertLocal(Element element) {
    element.setAttribute("type", JavascriptDebugConfigurationType.getTypeInstance().getId());
    element.setAttribute("singleton", "true");
    element.removeAttribute("factoryName");
    String uri = null;
    for (Object obj : element.getChildren("option")) {
      if ("fileUrl".equals(((Element)obj).getAttributeValue("name"))) {
        uri = ((Element)obj).getAttributeValue("value");
      }
    }
    element.removeContent();
    if (!StringUtil.isEmptyOrSpaces(uri)) {
      element.setAttribute("uri", uri.trim());
    }
  }

  private static void convertRemote(Element element) {
    element.setAttribute("type", JavascriptDebugConfigurationType.getTypeInstance().getId());
    element.setAttribute("singleton", "true");
    element.removeAttribute("factoryName");
    String uri = null;
    Element mappings = null;
    for (Object obj : element.getChildren("option")) {
      if ("fileUrl".equals(((Element)obj).getAttributeValue("name"))) {
        uri = ((Element)obj).getAttributeValue("value");
      }
      else if ("mappings".equals(((Element)obj).getAttributeValue("name"))) {
        mappings = (Element)obj;
      }
    }
    element.removeContent();
    if (!StringUtil.isEmptyOrSpaces(uri)) {
      element.setAttribute("uri", uri.trim());
    }
    if (mappings != null) {
      Element list = mappings.getChild("list");
      if (list != null) {
        element.addContent(list.removeContent());
      }
    }
  }

  public static boolean isConversionNeeded(Element element) {
    return "DartConfigurationType".equalsIgnoreCase(element.getAttributeValue("type")) ||
           "DartDebugSession".equalsIgnoreCase(element.getAttributeValue("type"));
  }
}
