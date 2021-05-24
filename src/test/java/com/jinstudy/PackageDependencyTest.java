package com.jinstudy;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packagesOf = App.class)
public class PackageDependencyTest { // 패키지간의 의존 관계(아키텍처) 테스트

    private static final String STUDY = "..modules.study..";
    private static final String ACCOUNT = "..modules.account..";
    private static final String EVENT = "..modules.event..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";

    @ArchTest
    ArchRule studyPackageRule = classes().that().resideInAPackage(STUDY)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(STUDY,EVENT);
    // study 패키지 안에 있는 클래스들은 study 패키지 또는 event 패키지 내부의 클래스들에 의해서만 참조될 수 있어야함 -> 이에 대한 테스트를 진행하는 것.

    @ArchTest
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat().resideInAnyPackage(EVENT, STUDY, ACCOUNT);
    //event 패키지 안에 있는 클래스들은 event, study, account 패키지 내부의 클래스들을 참조할 수 있다.

    @ArchTest
    ArchRule accountPackageRule = classes().that().resideInAPackage(ACCOUNT)
            .should().accessClassesThat().resideInAnyPackage(ACCOUNT,TAG,ZONE);

    @ArchTest
    ArchRule circleCheck = slices().matching("com.jinstudy.modules.(*)..")
            .should().beFreeOfCycles();
    // modules 패키지 내부의 모든 패키지들간에 순환 참조가 있으면 안된다.

    @ArchTest
    ArchRule modulesPackageRule = classes().that().resideInAPackage("com.jinstudy.modules..")
            .should().onlyBeAccessed().byClassesThat().resideInAnyPackage("com.jinstudy.modules..");
    // modules 패키지 내부의 클래스들은 modules 패키지 내부의 클래스들에 의해서만 참조가 되어야한다.
    // 즉, Infra 패키지나  다른 패키지의 클래스에 의해서 참조되면 안된다.
}
