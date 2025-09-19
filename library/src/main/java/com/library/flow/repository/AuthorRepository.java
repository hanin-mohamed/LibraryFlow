package com.library.flow.repository;

import com.library.flow.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

import java.util.*;

public interface AuthorRepository extends JpaRepository<Author, UUID> {
}



