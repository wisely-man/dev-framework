(window.webpackJsonp=window.webpackJsonp||[]).push([["chunk-23097b02"],{1928:function(e,t,a){"use strict";a("733b")},"733b":function(e,t,a){},b9f6:function(e,t,a){"use strict";a.r(t);var i=a("bf46"),s=a("86a2"),r=a("7d0b"),l=a("2ef0"),n=a.n(l);const o=new Map,c=(e,t,a)=>({workingArea:new Map,treelist:new Map,tabName:e,apiUrl:t,params:a,isRenderTree:!1,showSearchArea:!1,hasSerach:!1});o.set("partyPerson",c("党支部人员","listDeptPerson",{type:"12001-20"})),o.set("deptPerson",c("部门人员","listDeptPerson",{type:"12001-10"})),o.set("party",c("党支部","getListDeptTree",{type:"12001-20"})),o.set("dept",c("部门","getListDeptTree",{type:"12001-10"})),o.set("docUnit",c("来文单位","getListDeptTree",{type:"12001-30",subType:"12009-10"})),o.set("insureUnit",c("保险公司","getListDeptTree",{type:"12001-30",subType:"12009-20"}));var h={props:{tabList:{type:Array,default:()=>[]},showComponent:{type:Boolean,default:!1},title:{type:String,default:""},defaultIds:{type:String,default:""}},components:{Dialog:i.a,choice:s.default,publicCom:r.default},data:()=>({controlData:new Map,DBtnText:"确 定",activeTab:"",allData:new Map,commonIds:new Set,publicSelect:[],tabConfig:[],CONTROL:o,searchValue:"",renderPerson:!1}),created(){this.init()},methods:{init(){this.tabList.forEach(e=>{let t=this.CONTROL.get(e);t&&(t.type=e,this.tabConfig.push(n.a.cloneDeep(t)))}),this.renderPerson="partyPerson"==this.tabConfig[0].type||"deptPerson"==this.tabConfig[0].type,this.setDialogIcon()},setDialogIcon(){let e=this.tabList[0];this.diglogIconfont="partyPerson"==e||"deptPerson"==e?"el-icon-aliuser":"el-icon-alistructure"},handleTrueClick(){if(!this.controlData.size)return void this.$showWarning("请选择"+this.title);let e=[];for(let t of this.controlData.values())e.push(t);this.$emit("trueClick",e)},handleCancelClick(){this.$emit("cancelClick")},handleGetSelected(e,t){let a=new Map;a.set("add","addControlData"),a.set("del","delControlData"),a.set("clear","clearControlData"),this[a.get(t)](e)},setWorking(e,t){if(this.tabConfig[this.activeTab].workingArea=new Map(e),!this.commonIds.size)return;let a=[],i=[];for(let s of this.commonIds.keys())t.has(s)&&(e.has(s)&&a.push(e.get(s)),!e.has(s)&&i.push(s));this.setPublicIds(a,!0),this.setPublicIds(i,!1)},chekcIds(e){let t=new Set,a=this.defaultIds.split(","),i=n.a.cloneDeep(this.controlData);e.obj.forEach(a=>{this.allData.has(a.id)?t.add(a.id):this.allData.set(a.id,a),this.tabConfig[e.type].treelist.set(a.id,a)}),a.forEach(t=>{let a=this.allData.get(Number(t));a&&i.set(a.id,n.a.cloneDeep(a))&&this.tabConfig[e.type].workingArea.set(a.id,a)}),t.size&&(this.commonIds=new Set(t)),i.size&&(this.controlData=new Map(i)),this.tabConfig[this.activeTab].isRenderTree=!0},handleTabClick(){this.tabConfig[this.activeTab].isRenderTree=!0},setControlData(e){this.controlData=new Map(e)},setPublicSelect(e,t){t?this.publicSelect.push(e):this.publicSelect.forEach((t,a)=>{e.id==t.id&&this.publicSelect.splice(a,1)});let a=this.controlData;a.get(e.id).isZzSelected=t,this.controlData=new Map(a)},allZzSelected(){let e=n.a.cloneDeep(this.allData);for(let t of e.values())t.isZzSelected=!1;this.controlData=new Map(e),this.tabConfig.forEach((e,t)=>{let a=n.a.cloneDeep(e.treelist);this.tabConfig[t].workingArea=new Map(a)}),this.tabConfig[this.activeTab].isRenderTree=!0},clearAllPublic(){this.controlData=new Map,this.tabConfig.forEach((e,t)=>{this.tabConfig[t].workingArea=new Map}),this.tabConfig[this.activeTab].isRenderTree=!0},selectedZzClick(e,t){if(this.$refs[t+"public"][0].showselectedZz=!1,this.$refs[t+"public"][0].selectedZzValue="",this.$refs[t][0].showSearchZz){let e=new Map;return this.$refs[t][0].searchOrganizationList.forEach(t=>{t.isZzSelected&&e.set(t.id,n.a.cloneDeep(t))}),void this.handleGetSelected(e,"add")}e.size&&this.handleGetSelected(e,"add")},removeZzSelected(e){this.$refs[e][0].showSearchZz=!1,this.$refs[e][0].organizationValue="";let t=[];for(let e of this.controlData.values())e.isZzSelected&&t.push(e);this.handleGetSelected(t,"del")},addControlData(e){let t=n.a.cloneDeep(this.controlData),a=this.tabConfig[this.activeTab],i=this.tabConfig[this.activeTab].treelist;for(let e of i.keys())t.get(e)&&t.delete(e);if(a.hasSerach){let e=new Map(this.tabConfig[this.activeTab].workingArea);this.$refs[this.activeTab][0].searchOrganizationList.forEach(t=>{if(t.isZzSelected){let a=n.a.cloneDeep(t);a.isZzSelected=!1,e.set(t.id,a)}});for(let[a,i]of n.a.cloneDeep(e))i.isZzSelected=!1,t.set(a,i);this.tabConfig[this.activeTab].workingArea=new Map(e)}else{if(!a.workingArea.size)return;for(let[e,i]of n.a.cloneDeep(a.workingArea))i.isZzSelected=!1,t.set(e,i)}this.controlData=new Map(t),this.tabConfig[this.activeTab].isRenderTree=!0},delControlData(e){let t=n.a.cloneDeep(this.controlData);if(Array.isArray(e)){let a=[];e.forEach(e=>{t.delete(e.id),a.push(e.id)}),this.delWorkingArea(a)}else t.delete(e.id),this.delWorkingArea(e.id);this.controlData=new Map(t),this.tabConfig[this.activeTab].isRenderTree=!0},clearControlData(e){this.controlData.clear()},delWorkingArea(e){this.tabConfig.forEach((t,a)=>{let i=n.a.cloneDeep(t.workingArea);if(Array.isArray(e))e.forEach(e=>{t.workingArea.has(e)&&i.delete(e)});else{if(!t.workingArea.has(e))return;i.delete(e)}this.tabConfig[a].workingArea=new Map(i)})},setPublicIds(e,t){this.tabConfig.forEach((a,i)=>{let s=n.a.cloneDeep(a.workingArea);e.forEach(e=>{t?s.set(e.id,e):s.delete(e)}),this.tabConfig[i].workingArea=new Map(s)})}},watch:{controlData:{handler(e){let t=e.size;t?this.$set(this,"trueText",`确 定(${t})`):this.$set(this,"trueText","确 定")}}}},d=(a("1928"),a("2877")),p=Object(d.a)(h,(function(){var e=this,t=e._self._c;return t("div",[t("Dialog",{staticClass:"select-choice",attrs:{iconfont:"el-icon-alipeople-tit",dialogVisible:e.showComponent,title:e.title,trueText:e.DBtnText},on:{cancelClick:e.handleCancelClick,trueClick:e.handleTrueClick}},[t("el-tabs",{on:{"tab-click":e.handleTabClick},model:{value:e.activeTab,callback:function(t){e.activeTab=t},expression:"activeTab"}},e._l(e.tabConfig,(function(a,i){return t("el-tab-pane",{key:i,attrs:{label:a.tabName,name:i.toString()}},[t("el-row",{staticClass:"personal-choice",attrs:{gutter:24}},[t("el-col",{staticClass:"col-zz",staticStyle:{"padding-left":"12px","padding-right":"12px"},attrs:{span:11}},[t("choice",{ref:i,refInFor:!0,attrs:{dialogVisible:e.showComponent,controlData:e.controlData,workingArea:a.workingArea,apiurl:a.apiUrl,params:a.params,isRenderTree:a.isRenderTree,type:i,searchArea:a.searchArea,hasSerach:a.hasSerach,showSearchArea:a.showSearchArea,isActived:e.activeTab==i,searchValue:e.searchValue,renderType:a.type},on:{"update:isRenderTree":function(t){return e.$set(a,"isRenderTree",t)},"update:is-render-tree":function(t){return e.$set(a,"isRenderTree",t)},"update:hasSerach":function(t){return e.$set(a,"hasSerach",t)},"update:has-serach":function(t){return e.$set(a,"hasSerach",t)},"update:showSearchArea":function(t){return e.$set(a,"showSearchArea",t)},"update:show-search-area":function(t){return e.$set(a,"showSearchArea",t)},"update:searchValue":function(t){e.searchValue=t},"update:search-value":function(t){e.searchValue=t},chekcIds:e.chekcIds,setWorking:e.setWorking,selected:e.handleGetSelected}})],1),t("el-col",{staticStyle:{"padding-left":"12px","padding-right":"12px"},attrs:{span:2}},[t("div",{staticClass:"dv-btn"},[t("span",{staticClass:"s-right",style:{background:a.workingArea.size||a.hasSerach?"#118AF7":"#ccc"},on:{click:function(t){return e.selectedZzClick(a.workingArea,i)}}},[t("i",{staticClass:"el-icon-aliarrright"})]),t("span",{staticClass:"s-right s-left",style:{background:e.publicSelect.length?"#118AF7":"#ccc"},on:{click:function(t){return e.removeZzSelected(i)}}},[t("i",{staticClass:"el-icon-aliarrleft"})]),t("span",{staticClass:"s-right r-export",style:{background:a.showSearchArea?"#ccc":"#118AF7"},on:{click:e.allZzSelected}},[t("i",{staticClass:"el-icon-aliexport"})]),t("span",{staticClass:"s-right l-export",style:{background:!a.showSearchArea&&e.controlData.size>0||!a.showSearchArea&&e.controlData.size>0?"#118AF7":"#ccc"},on:{click:e.clearAllPublic}},[t("i",{staticClass:"el-icon-aliexport"})])])]),t("el-col",{staticClass:"col-zz",staticStyle:{"padding-left":"12px","padding-right":"12px"},attrs:{span:11}},[t("publicCom",{ref:i+"public",refInFor:!0,attrs:{renderType:a.type,controlData:e.controlData},on:{setPublicSelect:e.setPublicSelect,setControlData:e.setControlData,selected:e.handleGetSelected}})],1)],1)],1)})),1)],1)],1)}),[],!1,null,"46374cd4",null);t.default=p.exports}}]);