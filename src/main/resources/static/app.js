const API_BASE = "/api/testcases";
const PAGE_SIZE = 8;

const state = {
  cases: [],
  filtered: [],
  page: 1,
  statusFilter: "ALL",
  priorityFilter: "ALL",
  moduleFilter: "ALL",
  keyword: ""
};

const demoCases = [
  {
    title: "登录成功后进入首页",
    module: "用户中心",
    priority: "P0",
    status: "READY",
    creator: "demo-user",
    precondition: "用户账号正常，密码正确",
    steps: "1. 打开登录页\n2. 输入账号密码\n3. 点击登录",
    expectedResult: "登录成功并进入首页"
  },
  {
    title: "库存充足时提交订单成功",
    module: "订单管理",
    priority: "P0",
    status: "DRAFT",
    creator: "demo-user",
    precondition: "商品库存大于 1",
    steps: "1. 选择商品\n2. 提交订单\n3. 确认支付前订单状态",
    expectedResult: "订单创建成功，状态为待支付"
  },
  {
    title: "余额不足时支付失败提示明确",
    module: "支付中心",
    priority: "P1",
    status: "READY",
    creator: "demo-user",
    precondition: "用户余额小于订单金额",
    steps: "1. 创建订单\n2. 使用余额支付",
    expectedResult: "支付失败，并提示余额不足"
  },
  {
    title: "测试报告导出权限校验",
    module: "报表中心",
    priority: "P2",
    status: "PASSED",
    creator: "demo-user",
    precondition: "当前用户拥有测试主管角色",
    steps: "1. 进入报表页\n2. 点击导出",
    expectedResult: "成功下载测试报告"
  }
];

const el = {
  apiStatusDot: document.querySelector("#apiStatusDot"),
  apiStatusText: document.querySelector("#apiStatusText"),
  totalCount: document.querySelector("#totalCount"),
  highPriorityCount: document.querySelector("#highPriorityCount"),
  moduleCount: document.querySelector("#moduleCount"),
  caseTableBody: document.querySelector("#caseTableBody"),
  emptyState: document.querySelector("#emptyState"),
  keywordInput: document.querySelector("#keywordInput"),
  priorityFilter: document.querySelector("#priorityFilter"),
  moduleFilter: document.querySelector("#moduleFilter"),
  pageSummary: document.querySelector("#pageSummary"),
  prevPageBtn: document.querySelector("#prevPageBtn"),
  nextPageBtn: document.querySelector("#nextPageBtn"),
  caseDialog: document.querySelector("#caseDialog"),
  caseForm: document.querySelector("#caseForm"),
  dialogTitle: document.querySelector("#dialogTitle"),
  toast: document.querySelector("#toast"),
  smokeLog: document.querySelector("#smokeLog"),
  commandPreview: document.querySelector("#commandPreview")
};

document.addEventListener("DOMContentLoaded", () => {
  bindEvents();
  loadCases();
});

function bindEvents() {
  document.querySelector("#refreshBtn").addEventListener("click", loadCases);
  document.querySelector("#seedBtn").addEventListener("click", seedDemoData);
  document.querySelector("#openCreateBtn").addEventListener("click", () => openDialog());
  document.querySelector("#closeDialogBtn").addEventListener("click", closeDialog);
  document.querySelector("#cancelFormBtn").addEventListener("click", closeDialog);
  document.querySelector("#runSmokeBtn").addEventListener("click", runSmokeFlow);
  document.querySelectorAll(".copy-command").forEach((button) => {
    button.addEventListener("click", () => copyCommand(button.dataset.command));
  });

  el.keywordInput.addEventListener("input", (event) => {
    state.keyword = event.target.value.trim();
    state.page = 1;
    applyFilters();
  });

  el.priorityFilter.addEventListener("change", (event) => {
    state.priorityFilter = event.target.value;
    state.page = 1;
    applyFilters();
  });

  el.moduleFilter.addEventListener("change", (event) => {
    state.moduleFilter = event.target.value;
    state.page = 1;
    applyFilters();
  });

  document.querySelectorAll("[data-status-filter]").forEach((button) => {
    button.addEventListener("click", () => {
      document.querySelectorAll("[data-status-filter]").forEach((item) => item.classList.remove("active"));
      button.classList.add("active");
      state.statusFilter = button.dataset.statusFilter;
      state.page = 1;
      applyFilters();
    });
  });

  el.prevPageBtn.addEventListener("click", () => {
    if (state.page > 1) {
      state.page -= 1;
      renderTable();
    }
  });

  el.nextPageBtn.addEventListener("click", () => {
    const maxPage = Math.max(1, Math.ceil(state.filtered.length / PAGE_SIZE));
    if (state.page < maxPage) {
      state.page += 1;
      renderTable();
    }
  });

  el.caseForm.addEventListener("submit", (event) => {
    event.preventDefault();
    saveCase();
  });
}

async function request(path = "", options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    ...options
  });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok || payload.code >= 400) {
    throw new Error(payload.message || `请求失败：${response.status}`);
  }
  return payload.data;
}

async function loadCases() {
  try {
    setApiStatus("loading", "正在读取后端数据...");
    const data = await request();
    state.cases = Array.isArray(data) ? data : [];
    setApiStatus("ok", "后端连接正常，数据已同步。");
    rebuildModuleFilter();
    applyFilters();
  } catch (error) {
    state.cases = [];
    setApiStatus("bad", `后端不可用：${error.message}`);
    rebuildModuleFilter();
    applyFilters();
  }
}

function applyFilters() {
  const keyword = state.keyword.toLowerCase();
  state.filtered = state.cases.filter((item) => {
    const title = normalize(item.title).toLowerCase();
    const moduleName = normalize(item.module).toLowerCase();
    const status = normalize(item.status || "DRAFT");
    const priority = normalize(item.priority);
    const keywordMatched = !keyword || title.includes(keyword) || moduleName.includes(keyword);
    const statusMatched = state.statusFilter === "ALL" || status === state.statusFilter;
    const priorityMatched = state.priorityFilter === "ALL" || priority === state.priorityFilter;
    const moduleMatched = state.moduleFilter === "ALL" || normalize(item.module) === state.moduleFilter;
    return keywordMatched && statusMatched && priorityMatched && moduleMatched;
  });

  renderMetrics();
  renderTable();
}

function renderMetrics() {
  const modules = new Set(state.cases.map((item) => normalize(item.module)).filter(Boolean));
  const highPriority = state.cases.filter((item) => ["P0", "P1"].includes(normalize(item.priority)));
  el.totalCount.textContent = state.cases.length;
  el.highPriorityCount.textContent = highPriority.length;
  el.moduleCount.textContent = modules.size;
}

function renderTable() {
  const start = (state.page - 1) * PAGE_SIZE;
  const rows = state.filtered.slice(start, start + PAGE_SIZE);
  el.caseTableBody.innerHTML = rows.map(renderRow).join("");

  el.emptyState.classList.toggle("hidden", state.filtered.length > 0);
  document.querySelectorAll("[data-edit-id]").forEach((button) => {
    button.addEventListener("click", () => openDialog(findCase(button.dataset.editId)));
  });
  document.querySelectorAll("[data-delete-id]").forEach((button) => {
    button.addEventListener("click", () => deleteCase(button.dataset.deleteId));
  });

  const end = Math.min(start + rows.length, state.filtered.length);
  el.pageSummary.textContent = `${state.filtered.length ? start + 1 : 0}-${end} / ${state.filtered.length}`;
  el.prevPageBtn.disabled = state.page <= 1;
  el.nextPageBtn.disabled = state.page >= Math.max(1, Math.ceil(state.filtered.length / PAGE_SIZE));
}

function renderRow(item) {
  const priority = normalize(item.priority || "P2");
  const status = normalize(item.status || "DRAFT");
  return `
    <tr>
      <td>
        <div class="case-title">${escapeHtml(item.title)}</div>
        <div class="subtle">ID: ${escapeHtml(item.id)}</div>
      </td>
      <td>${escapeHtml(item.module || "-")}</td>
      <td><span class="tag priority-${priority.toLowerCase()}">${escapeHtml(priority)}</span></td>
      <td><span class="tag">${escapeHtml(status)}</span></td>
      <td>${escapeHtml(item.creator || "-")}</td>
      <td>${formatTime(item.updateTime || item.createTime)}</td>
      <td>
        <div class="row-actions">
          <button class="btn ghost" type="button" data-edit-id="${escapeHtml(item.id)}">编辑</button>
          <button class="btn ghost" type="button" data-delete-id="${escapeHtml(item.id)}">删除</button>
        </div>
      </td>
    </tr>
  `;
}

function rebuildModuleFilter() {
  const modules = [...new Set(state.cases.map((item) => normalize(item.module)).filter(Boolean))].sort();
  el.moduleFilter.innerHTML = `<option value="ALL">全部模块</option>${modules
    .map((moduleName) => `<option value="${escapeHtml(moduleName)}">${escapeHtml(moduleName)}</option>`)
    .join("")}`;
  state.moduleFilter = "ALL";
}

function openDialog(item = null) {
  el.caseForm.reset();
  document.querySelector("#caseId").value = item?.id || "";
  document.querySelector("#titleInput").value = item?.title || "";
  document.querySelector("#moduleInput").value = item?.module || "";
  document.querySelector("#priorityInput").value = item?.priority || "P1";
  document.querySelector("#statusInput").value = item?.status || "DRAFT";
  document.querySelector("#creatorInput").value = item?.creator || "demo-user";
  document.querySelector("#preconditionInput").value = item?.precondition || "";
  document.querySelector("#stepsInput").value = item?.steps || "";
  document.querySelector("#expectedResultInput").value = item?.expectedResult || "";
  el.dialogTitle.textContent = item ? "编辑用例" : "新建用例";
  el.caseDialog.showModal();
}

function closeDialog() {
  el.caseDialog.close();
}

async function saveCase() {
  const id = document.querySelector("#caseId").value;
  const body = collectFormData();
  try {
    if (id) {
      await request(`/${id}`, {
        method: "PUT",
        body: JSON.stringify(body)
      });
      showToast("用例已更新");
    } else {
      await request("", {
        method: "POST",
        body: JSON.stringify(body)
      });
      showToast("用例已创建");
    }
    closeDialog();
    await loadCases();
  } catch (error) {
    showToast(error.message);
  }
}

function collectFormData() {
  return {
    title: document.querySelector("#titleInput").value.trim(),
    module: document.querySelector("#moduleInput").value.trim(),
    priority: document.querySelector("#priorityInput").value,
    status: document.querySelector("#statusInput").value,
    creator: document.querySelector("#creatorInput").value.trim(),
    precondition: document.querySelector("#preconditionInput").value.trim(),
    steps: document.querySelector("#stepsInput").value.trim(),
    expectedResult: document.querySelector("#expectedResultInput").value.trim()
  };
}

async function deleteCase(id) {
  if (!window.confirm("确认删除这条用例？")) {
    return;
  }
  try {
    await request(`/${id}`, { method: "DELETE" });
    showToast("用例已删除");
    await loadCases();
  } catch (error) {
    showToast(error.message);
  }
}

async function seedDemoData() {
  try {
    const existingTitles = new Set(state.cases.map((item) => normalize(item.title)));
    const missingCases = demoCases.filter((item) => !existingTitles.has(item.title));
    for (const item of missingCases) {
      await request("", {
        method: "POST",
        body: JSON.stringify(item)
      });
    }
    showToast(missingCases.length ? `已新增 ${missingCases.length} 条演示数据` : "演示数据已存在");
    await loadCases();
  } catch (error) {
    showToast(error.message);
  }
}

async function runSmokeFlow() {
  renderSmokeLog(["开始执行接口冒烟流程"]);
  try {
    const created = await request("", {
      method: "POST",
      body: JSON.stringify({
        title: `面试演示冒烟用例 ${Date.now()}`,
        module: "演示模块",
        priority: "P1",
        status: "DRAFT",
        creator: "interview-demo",
        precondition: "后端服务可访问",
        steps: "调用创建、查询、更新接口",
        expectedResult: "所有接口返回 code=200"
      })
    });
    renderSmokeLog(["创建用例成功", `生成 ID：${created.id}`]);

    const found = await request(`/${created.id}`);
    renderSmokeLog(["创建用例成功", `生成 ID：${created.id}`, `查询成功：${found.title}`]);

    await request(`/${created.id}`, {
      method: "PUT",
      body: JSON.stringify({
        ...found,
        status: "PASSED",
        title: `${found.title} - PASSED`
      })
    });
    renderSmokeLog(["创建用例成功", `生成 ID：${created.id}`, `查询成功：${found.title}`, "更新状态成功"]);

    const updated = await request(`/${created.id}`);
    if (updated.status !== "PASSED") {
      throw new Error("更新结果校验失败");
    }
    renderSmokeLog([
      "创建用例成功",
      `生成 ID：${created.id}`,
      `查询成功：${found.title}`,
      "更新状态成功",
      "更新结果校验通过"
    ]);

    await request(`/${created.id}`, { method: "DELETE" });
    renderSmokeLog([
      "创建用例成功",
      `生成 ID：${created.id}`,
      `查询成功：${found.title}`,
      "更新状态成功",
      "更新结果校验通过",
      "删除清理成功"
    ]);

    await loadCases();
    showToast("接口冒烟流程通过");
  } catch (error) {
    renderSmokeLog(["接口冒烟失败", error.message]);
    showToast(error.message);
  }
}

async function copyCommand(command) {
  el.commandPreview.textContent = command;
  try {
    await navigator.clipboard.writeText(command);
    showToast("命令已复制到剪贴板");
  } catch (error) {
    showToast("浏览器限制剪贴板权限，请手动选中命令复制");
  }
}

function renderSmokeLog(items) {
  el.smokeLog.innerHTML = items.map((item) => `<li>${escapeHtml(item)}</li>`).join("");
}

function findCase(id) {
  return state.cases.find((item) => String(item.id) === String(id));
}

function setApiStatus(type, text) {
  el.apiStatusDot.className = `status-dot ${type === "ok" ? "ok" : type === "bad" ? "bad" : ""}`;
  el.apiStatusText.textContent = text;
}

function showToast(message) {
  el.toast.textContent = message;
  el.toast.classList.remove("hidden");
  window.clearTimeout(showToast.timer);
  showToast.timer = window.setTimeout(() => {
    el.toast.classList.add("hidden");
  }, 2600);
}

function normalize(value) {
  return value == null ? "" : String(value).trim();
}

function formatTime(value) {
  if (!value) {
    return "-";
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return String(value).replace("T", " ");
  }
  return date.toLocaleString("zh-CN", {
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit"
  });
}

function escapeHtml(value) {
  return normalize(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}
