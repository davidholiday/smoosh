package com.projectvalis.jbehave;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.StoryFinder;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;
import org.jbehave.core.steps.Steps;
import org.junit.runner.RunWith;

import com.projectvalis.reverse_rabin.DialecticCompressionSteps;
import com.projectvalis.reverse_rabin.DoubleHashSteps;
import com.projectvalis.reverse_rabin.FurlSteps;
import com.projectvalis.reverse_rabin.ReverseRabinSteps;

import de.codecentric.jbehave.junit.monitoring.JUnitReportingRunner;



/**
 * generic binder for all JBehave tests. Binds all the story files to the 
 * step files. works for both Eclipse and Maven command line build.  
 * @author funktapuss
 *
 */
@RunWith(JUnitReportingRunner.class)
public class JBehaveRunner_Test extends JUnitStories {

    @Override 
    public Configuration configuration() { 
        return new MostUsefulConfiguration()            
                .useStoryLoader(
                        new LoadFromClasspath(this.getClass().getClassLoader()))
                .useStoryReporterBuilder(
                        new StoryReporterBuilder()
                            .withDefaultFormats()
                            .withFormats(Format.HTML, Format.CONSOLE)
                            .withRelativeDirectory("jbehave-report")
                );
    }

    @Override
    public InjectableStepsFactory stepsFactory() {
        ArrayList<Steps> stepFileList = new ArrayList<Steps>();
        //stepFileList.add(new FurlSteps());
        //stepFileList.add(new ReverseRabinSteps());
        //stepFileList.add(new DialecticCompressionSteps());
        stepFileList.add(new DoubleHashSteps());
        return new InstanceStepsFactory(configuration(), stepFileList);       
    }

    
    @Override
    protected List<String> storyPaths() {
       return new StoryFinder().
               findPaths(CodeLocations.codeLocationFromClass(
                       this.getClass()), 
                       Arrays.asList("**/*.story"), 
                       Arrays.asList(""));

    }
    
    
}
