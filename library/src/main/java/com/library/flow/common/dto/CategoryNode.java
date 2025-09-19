package com.library.flow.common.dto;

import java.util.List;
import java.util.UUID;

public record CategoryNode(UUID id, String name, UUID parentId, List<CategoryNode> children) {}
