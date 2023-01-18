package org.cloudfoundry.jdbctestapp

import org.springframework.data.repository.CrudRepository

interface UserRepository: CrudRepository<User, Int>