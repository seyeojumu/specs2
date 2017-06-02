package org.specs2
package specification
package process

object StatisticsRepositoryStore {

  def memory = StatisticsRepository(StatisticsMemoryStore())

}


