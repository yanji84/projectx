package com.projectx.automa.plan.stage

import com.projectx.automa.plan.PlanExecutionContext
import com.projectx.automa.plan.Plan
import com.projectx.automa.plan.strategy._
import com.projectx.automa.plan.step._

/**
*
* File Name: EnsembleStage.scala
* Date: Feb 19, 2016
* Author: Ji Yan
*
*/

class EnsembleStage extends Stage {
	val ensembleSteps = List[EnsembleStep]()
	override def buildPlan(plan:Plan, executionContext:PlanExecutionContext):Unit = {
		val strategy = new Parallel
		for (ensembleStep <- ensembleSteps) {
			if (ensembleStep.check(plan, executionContext)) {
				strategy.addStepToPlan(ensembleStep, plan)
			}
		}
	}
}